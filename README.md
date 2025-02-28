# Thin Client REST Application
REST APIs are exposed using a thin client in the branch ThinClientREST. By starting ignite and loading the data (as mentioned in the above steps), this branch can be directly used for the REST APIs. 
(Reference- step no.8 of the spring-data-training).

Run Ignite 2.x cluster by running ```./ignite.sh``` from the $IGNITE_HOME directory.

  1. Clone this branch of the Project

      ```bash
      git clone -b ThinClientREST https://github.com/GridGain-Demos/spring-data-training.git
      ```
      
  2. Build a shaded package for the app:
      ```shell script
      mvn clean package
      ```

  3. Start an SQLLine process:

      ```shell script
      java -cp libs/app.jar sqlline.SqlLine
      ```

  4. Connect to the cluster:

      ```shell script
      !connect jdbc:ignite:thin://127.0.0.1/ ignite ignite
      ```

  5. Load the database:

      ```shell script
      !run config/world.sql
      ```
      
  6. From another terminal, run the project's unit tests. The tests should succeed. Any failures are indication of Ignite not being reachable

      ```shell script
      mvn test
      ```      
  7. Run the application. This should show Spring project started with port number.

      ```shell script
      mvn spring-boot:run
      ```
  8. Open Postman and run the query. You may even use curl or any other tool.
    ```
      http://localhost:8080/api/mostPopulated?limit=6
    ```   


<img width="859" alt="image" src="https://github.com/user-attachments/assets/fdfde605-9194-4ad0-b378-73be0cedda23" />

