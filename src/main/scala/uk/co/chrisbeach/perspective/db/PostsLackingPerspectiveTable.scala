package uk.co.chrisbeach.perspective.db

import doobie.imports._
import doobie.util.fragments.{notIn, in}
import uk.co.chrisbeach.perspective.db.PostTypes.{PostId, RegularPostType, SystemUsers}

import scalaz.NonEmptyList
import scalaz.effect.IO

class PostsLackingPerspectiveTable(transactor: Transactor[IO],
                                   includePostsOfType: NonEmptyList[Int] = NonEmptyList(RegularPostType),
                                   excludeUsers: NonEmptyList[Int] = SystemUsers,
                                   userMinPostCount: Int = 10) {

  private val fromClause =
    fr"""
    FROM posts p
      JOIN topics t ON t.id = p.topic_id
      JOIN users u ON u.id = p.user_id
      JOIN user_stats s ON s.user_id = u.id
      LEFT JOIN perspective pe ON pe.post_id = p.id
    WHERE""" ++
      notIn(fr"p.user_id", excludeUsers) ++ fr"AND" ++
      in(fr"p.post_type", includePostsOfType) ++ fr"AND" ++
    fr"""
      pe.post_id IS NULL AND
      s.post_count > $userMinPostCount AND
      t.category_id IS NOT NULL"""

  def count(): Int =
    (fr"SELECT count(*)" ++ fromClause)
      .query[Int].unique.transact(transactor).unsafePerformIO()

  def posts(maxCount: Int): List[Post] =
    (fr"SELECT p.id AS id, raw AS comment" ++ fromClause ++ fr"ORDER BY p.id DESC LIMIT $maxCount")
      .query[Post].list.transact(transactor).unsafePerformIO()
}

case class Post(id: PostId, comment: String)

object PostTypes {
  type PostId = Int

  val RegularPostType = 1
  val SystemUsers: NonEmptyList[Int] = NonEmptyList(-1, -2)
}