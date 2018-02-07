## Summary

Measure the civility of posts on your Discourse forum using
[Google Perspective API](https://www.perspectiveapi.com/)

See discussion here: https://meta.discourse.org/t/evaluating-googles-perspective-api-on-your-discourse-forum/79971

#### Notes:

* **Requires Docker to be installed**
* **You'll need an API key** (see https://www.perspectiveapi.com/)
* Only users with a post_count of 10+ are considered
* Posts are truncated to fit the Perspective API's 3,000
character limit

## 1. Start Database Server, Populated From Discourse Backup

Browse to `/admin/backups` on your forum and click **Backup**. Select
"Yes (do not include files)." Click **Download** and you'll be mailed a
link to the backup file.

Now run the following command in the root of this project:

```
./db.sh [ABSOLUTE PATH TO YOUR BACKUP FILE].sql
```

Wait as your database is imported. The message "PostgreSQL init process
complete; ready for start up" will then be displayed. Postgres is
ready to go (it's a foreground task, please leave it running for now).

## 2. Run App

In another terminal, run the following command in the root of this
project:

```
./run.sh [YOUR API KEY]
```

This will create a table in the running Postgres database, query the
Google Perspective API and populate the table with the results.
Given the rate limits (10/sec), this may take some time.

## 3. Analyse Results

Connect to the Postgres database using the SQL client of your choice:

* **Host:port/database:** localhost:5432/postgres
* **Username:** postgres
* **Password:** \[no password\]

Try one of the sample queries below:

### Example query for most toxic posters

```
SELECT
    '@' || u.username as "Username",
    round(avg(pe.toxicity)::numeric, 3) as "Average Toxicity",
    count(*) as "Number of Posts"
FROM
    posts p
    JOIN users u ON p.user_id = u.id
    JOIN perspective pe ON pe.post_id = p.id
GROUP BY u.username
ORDER BY avg(pe.toxicity) desc
LIMIT 100
```

### Example query for most toxic posts

```
SELECT
    '@' || u.username as "Username",
    round(pe.toxicity::numeric, 3) as "Toxicity",
    p.raw
FROM
    posts p
    JOIN users u ON p.user_id = u.id
    JOIN perspective pe ON pe.post_id = p.id
ORDER BY pe.toxicity desc
LIMIT 20
```

### Example query for overall site stats

```
SELECT
  round(avg(e.attack_on_author)::numeric, 3) as "Attack on Author",
  round(avg(e.attack_on_commenter)::numeric, 3) as "Attack on Commenter",
  round(avg(e.incoherent)::numeric, 3) as "Incoherent",
  round(avg(e.inflammatory)::numeric, 3) as "Inflammatory",
  round(avg(e.likely_to_reject)::numeric, 3) as "Likely to Reject",
  round(avg(e.obscene)::numeric, 3) as "Obscene",
  round(avg(e.severe_toxicity)::numeric, 3) as "Severe Toxicity",
  round(avg(e.spam)::numeric, 3) as "Spam",
  round(avg(e.toxicity)::numeric, 3) as "Toxicity",
  round(avg(e.unsubstantial)::numeric, 3) as "Unsubstantial"
FROM perspective e
```

## 4. Shutdown

To tidy up when finished (removes Docker container):

```
./shutdown.sh
```

## Troubleshooting

### Rate limit errors

Despite our best efforts to stay within Google's 1000 requests / 100 s
rate limits, sending bursty parallel requests sometimes causes
occasional rate limit breaches. To resolve, reduce the value of
`parallelism` in `src/main/resources/application.conf`

## Further Reference

https://github.com/conversationai/perspectiveapi/blob/master/api_reference.md#models

https://conversationai.github.io/

## Contributing

I'm new to Akka-HTTP, Docker, Doobie and the Perspective API.
I'd be grateful for your feedback and/or PRs.

## License

Apache 2.0