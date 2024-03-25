# Linchpino Service

This is the README file for the Linchpino Service application, explaining the environment variables used in the
configuration.

## Environment Variables

### Spring Application Name

- **Variable Name**: `SPRING_APPLICATION_NAME`
- **Description**: Specifies the name of the Spring Boot application.
- **Default Value**: `linchpino-service`

### PostgreSQL Configuration

- **Variable Name**: `POSTGRES_USERNAME`, `POSTGRES_PASSWORD`, `POSTGRES_URL`
- **Description**: Configures the PostgreSQL datasource properties.
    - `POSTGRES_USERNAME`: Specifies the username for connecting to PostgreSQL. Default value is `postgres`.
    - `POSTGRES_PASSWORD`: Specifies the password for connecting to PostgreSQL. Default value is `secret`.
    - `POSTGRES_URL`: Specifies the URL for connecting to PostgreSQL. Default value
      is `jdbc:postgresql://localhost:5432/linchpino`.

### JPA Configuration

- **Variable Name**: `HIBERNATE_SHOW_SQL`
- **Description**: Configures JPA and Hibernate properties.
    - `HIBERNATE_SHOW_SQL`: Specifies whether to log SQL statements. Default value is `true`.

#### Other Configuration

- **Description**: Additional configurations for JPA and Hibernate.
    - `database-platform`: Specifies the database dialect for Hibernate.
    - `hibernate.ddl-auto`: Specifies the automatic schema generation strategy for Hibernate.
    - `show-sql`: Specifies whether to log SQL statements.

## Starting the Application

### 1. Setting Environment Variables and Running Main Function in LinchpinApp.kt

- Set the required environment
  variables (`SPRING_APPLICATION_NAME`, `POSTGRES_USERNAME`, `POSTGRES_PASSWORD`, `POSTGRES_URL`, `HIBERNATE_SHOW_SQL`)
  according to your environment and requirements.
- Run the `main` function in the `LinchpinApp.kt` file to start the Spring Boot application.

### 2. Setting Environment Variables and Running Using Maven

- Set the required environment
  variables (`SPRING_APPLICATION_NAME`, `POSTGRES_USERNAME`, `POSTGRES_PASSWORD`, `POSTGRES_URL`, `HIBERNATE_SHOW_SQL`)
  according to your environment and requirements.
- Open a terminal and navigate to the project directory.
- Run the following Maven command to start the Spring Boot application:
    - `./mvnw spring-boot:run`
    - `mvn spring-boot:run`

### 3. Without Setting Any Environment Variables and Using Testcontainers

- Ensure that Docker is installed on your system.
- Run the `main` function in the `TestLinchpinApp.kt` file.
- Testcontainers will automatically start a PostgreSQL container with the required configuration, and the Spring Boot
  application will use it for database operations.

## Notes

- Ensure to set the environment variables accordingly before running the Linchpino Service application.
- Modify the default values of the environment variables as per your environment and requirements.
- Refer to the Spring Boot documentation for more information on configuring applications using environment variables.
