**This has been simplified to test baggage header propagation for ticket #2110364**

To test baggage header propadation, send a curl request to `localhost:8080`

```
# example request
curl -H "baggage: sd-routing-key=np90s7y0fxhfs" -H "ot-baggage: sd-routing-key=np90s7y0fxhfs" -H "tracestate: sd-routing-key=np90s7y0fxhfs" -H "traceparent: 00-68010c630000000015b60294e98428e4-10b5afcc542934f7-01" localhost:8080/
```

---

# APM: SpringBoot with Java and MongoDB

This sandbox spins up a basic CRUD (create, read, update, and delete) app built from the Initializr site in this SpringBoot Java tutorial [linked here](https://spring.io/guides/gs/accessing-data-mongodb/). This is meant to demonstrate what a sample application with tracing looks like if it makes mongo calls. **This sample application is not meant to be used in any production capacity.**

What's already set up:

- Java APM + SpringBoot + MongoDB app
- SpringBoot app (which uses servlet under the hood)
- MongoDB
- A linked volume pointing to the data folder so you can transfer files from and to the container.
- Unified service tagging has been set up (see Dockerfile)

## Sections

- Step 1: Configure the ENV file with your Datadog Agent API Key
- Step 2: Create an env file in this folder for your DATABASE credentials
- Step 3: Spin up Docker containers
- Step 4: Viewing Docker logs
- Step 5: Generating traces
- Step 6: Checking out the mongo container [optional]
- Step 7. To shut everything down
- Common Questions
- Development without an IDE (Advanced users only)
- Resources

### Step 1: Configure the ENV file with your Datadog Agent API Key

Make sure that in your `~` directory, you have a file called `sandbox.docker.env` that contains:

```
DD_API_KEY=<Your API Key>
```

Now when you run the next steps, you don't have to worry about your API Key in plain text somehow making its way into the repo.

### Step 2: Create an env file in this folder for your DATABASE credentials

First, make sure you're in `APM/Java/docker-springboot-java-mongo`.

Then create a `.env` in the directory of this README for the database credentials. To learn more about Docker environment variables, you can go through this [doc](https://docs.docker.com/compose/environment-variables/). Make sure you have this:

```
MONGODB_USER=<ENTER ANY DB USER>
MONGODB_PW=<ENTER ANY DB PW>
MONGODB_DBNAME=<ENTER ANY DB NAME YOU WANT>
```

If you skip this step, your database will not have the proper credentials and calls to it will fail.

If you're in a hurry, you can also just update the environment variables (**MONGODB_USER, MONGODB_PW, MONGODB_DBNAME**) in the `docker-compose.yml` files with any string you want. Be careful not to make a git commit with these keys though.

To verify that you did this correctly, run `docker-compose config` and the three keys that you configured will be replaced properly. The benefit is that you don't have to push your own unique password and database usernames accidentally into the repo.

### Step 3: Spin up Docker containers

1. Build the image: `docker-compose build`
2. Spin up the containers: `docker-compose up` or `docker-compose up -d` to run containers in the background.

### Step 4: Viewing Docker logs

These commands should be ran in a different terminal. It's useful to have the web and agent containers to see what's happening.

**Web container logs**

`docker logs -f docker-springboot-java-mongo_web_1`

**Datadog Agent container logs**

`docker logs -f docker-springboot-java-mongo_datadog-agent_1`

**MongoDB container logs**

`docker logs -f docker-springboot-java-mongo_mongo_1`

### Step 5: Generating traces

These are the endpoints you can curl, though you can visit the GET requests in your local browser via localhost instead of 0.0.0.0.

1. A welcome page:

```
curl "0.0.0.0:8080/"
```

2. To create a note:

```
curl -H 'Content-Type: application/json' -X POST "0.0.0.0:8080/notes" -d "hello world"

```

3. To get a note:

```
curl 0.0.0.0:8080/notes/id_string
```

4. To update a note:

```
curl -H 'Content-Type: application/json' -X PUT "0.0.0.0:8080/notes/id_string" -d "another description"
```

5. To delete a note:

```
curl  -X DELETE "0.0.0.0:8080/notes/id_string"
```

6. To view all notes:

```
curl 0.0.0.0:8080/notes
```

7. To create an internal server error (5xx status code):

```
curl -H 'Content-Type: application/json' -X PUT "0.0.0.0:8080/notes/1" -d "another description"
```

### Step 6: Checking out the mongo container [optional]

This part doesn't really have anything to do with traces since APM doesn't get installed here, but since databases are cool, here are some things you might want to know if you want to use this container to understand databases. The env file you used will be important to log into the mongo db, thanks to this [StackOverflow thread](https://stackoverflow.com/questions/28848840/mongodb-login-with-user-account) and this [StackOverflow thread on commands that you can run](https://stackoverflow.com/questions/26600847/viewing-mongo-db-contents-from-the-shell).

1. Exec into the db `docker exec -it docker-springboot-java-mongo_mongo_1 bash`
2. Log into mongo with `mongo -u <username from env file> -p <password from env file> --authenticationDatabase admin` . The admin here is a fixed value.
3. Once you're in, you can run mongo commands as usual.

**Examples:**

1. If you named your test database `storage` in the `.env`, then to switch into that database:

```
use storage
```

If you named your test database `test` in `.env`, then you would switch into it via:

```
use test
```

2. To check on existing collections once you ran `use db_name`:

```
db.getCollectionNames()
```

3. To view all notes created so far (if notes is a collection):

```
db.note.find()
```

### Step 7: To shut everything down

```
docker-compose down
```

And that's it!

### Common Questions

None yet, be sure to add any if you encounter issues with this that can be resolved without a PR!

### Development without an IDE (Advanced users only)

**Note: If you're making small code changes, you don't need to go through this section since the instructions above will suit most purposes.**

However, if you don't have an IDE and want to use this sandbox to develop Java applications live without building each time, you can also do the following:

**Step 1.** Update the docker-compose for `web` with `command: tail -f /dev/null`. This will keep the container running forevr. Example:

```
  web:
    build: .
    command: tail -f /dev/null
```

**Step 2.** `docker-compose build` and `docker-compose up -d` as usual.

**Step 3.** Exec into the container with `docker exec -it  docker-springboot-java-mongo_web_1 bash`.

**Step 4.** Inside the container, find the project in the linked folder, `cd /home/data/sample-mongo-app`

**Step 5.** Any code changes you make to the data folder in your local machine will be reflected in the linked folder. **Note: This means if you delete files in the docker container (`/home/data/sample-mongo-app`), you affect the files in your local machine so be careful.**

**Step 6.** Once you're done with code changes, `./mvnw -DskipTests=true package`

**Step 7.** Download a version of the java tracer https://github.com/DataDog/dd-trace-java/releases into the `/home/data/sample-mongo-app` folder, ie: `wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer`.

**Step 8.** `java -javaagent:dd-java-agent.jar -Ddd.trace.debug=true -jar target/sample-mongo-app-0.0.1-SNAPSHOT.jar`

### Resources:

1. https://spring.io/guides/gs/accessing-data-mongodb/
2. https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html
3. https://stackoverflow.com/questions/55135393/exception-authenticating-mongocredential-and-uncategorized-mongo-db-exception
4. https://stackoverflow.com/questions/49967316/crud-repository-findbyid-different-return-value
5. https://stackoverflow.com/questions/35531661/using-env-variable-in-spring-boots-application-properties
6. https://spring.io/guides/tutorials/rest/
7. https://stackoverflow.com/questions/28848840/mongodb-login-with-user-account
8. https://stackoverflow.com/questions/26600847/viewing-mongo-db-contents-from-the-shell
