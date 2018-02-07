package uk.co.chrisbeach.perspective

import java.lang.System.currentTimeMillis
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import uk.co.chrisbeach.perspective.PerspectiveClient.{allowedBatchSize, analysisTimeout, estimatedAnalysisDuration, rateLimitRenewalPeriod}
import uk.co.chrisbeach.perspective.db.PostTypes.PostId
import uk.co.chrisbeach.perspective.db.{PerspectiveTable, PostsLackingPerspectiveTable, Transactor}
import uk.co.chrisbeach.perspective.json.PerspectiveResponse
import uk.co.chrisbeach.perspective.util.JavaTimeConversion.toFiniteDuration

import scala.annotation.tailrec
import scala.collection.parallel.{ForkJoinTaskSupport, TaskSupport}
import scala.concurrent.Await
import scala.concurrent.forkjoin.ForkJoinPool
import scala.language.postfixOps

/**
  * Analyse posts in a Discourse DB using the Google Perspective API
  *
  * Save the results back into the DB into a table named 'perspective' for analysis
  *
  * @author Chris Beach
  */
object Runner extends LazyLogging {

  private val config = ConfigFactory.load()
  private val transactor = Transactor(config.getConfig("db"))
  private val postsLackingPerspective = new PostsLackingPerspectiveTable(transactor)
  private val perspective = new PerspectiveTable(transactor)

  /**
    * Note: with greater parallelism, Google occasionally reports API rate limit breaches,
    * despite us sticking to 1000 reqs / 100 s overall
    */
  private val parallelism: TaskSupport = new ForkJoinTaskSupport(new ForkJoinPool(config.getInt("parallelism")))

  private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  private val perspectiveClient = PerspectiveClient(
    apiKey = Option(System.getenv("API_KEY")).getOrElse(sys.error("Expected environment variable: API_KEY")),
    config = config.getConfig("api")
  )

  def main(args: Array[String]): Unit =
    try {
      logger.info("Ensuring perspective schema exists")
      perspective.createTableIfMissing()

      val postCount = postsLackingPerspective.count()

      logger.info(
        f"Starting analysis of $postCount%,d post(s) in batches of $allowedBatchSize, " +
        f"ETA: ${dateFormat.format(now.plus(estimatedAnalysisDuration(postCount)))}"
      )

      analysePosts(resultHandler = perspective.insert)

      logger.info("Analysis complete")
    } finally {
      perspectiveClient.shutdown()
    }

  /**
    * Request analysis from the Google Perspective API. Parallelised
    * Pausing as necessary to honour the rate limit
    */
  @tailrec
  private def analysePosts(resultHandler: (PostId, PerspectiveResponse) => Unit): Unit = {
    val posts = postsLackingPerspective.posts(allowedBatchSize).par

    posts.tasksupport = parallelism

    val start = currentTimeMillis()

    posts.foreach { post =>
      resultHandler(post.id, Await.result(perspectiveClient.analyse(post.comment), analysisTimeout))
    }
    print(".")

    if (posts.nonEmpty) {
      Thread.sleep(rateLimitRenewalPeriod.toMillis + start - currentTimeMillis() +
        config.getDuration("extraDelayBetweenBatches").toMillis)
      analysePosts(resultHandler)
    }
  }
}