# Kafka Broker Implementation in Java

[![progress-banner](https://backend.codecrafters.io/progress/kafka/d0cd83dd-f010-431f-8a68-0128ce23fb65)](https://app.codecrafters.io/users/codecrafters-bot?r=2qF)

This project is a Java-based implementation of a Kafka broker, designed as part of the [Codecrafters Kafka Challenge](https://codecrafters.io/challenges/kafka/overview). The goal is to create a functional Kafka broker that can handle client connections, process requests, and manage data distribution across a cluster.

## Current Progress

#### Basic Server Setup
- Implemented a basic server that listens on port 9092.
- Accepts client connections and handles requests in separate **threads**.

#### `APIVersions` Request Handling
- Implemented the response body for the `APIVersions` (v4) request.
- Validates the **message length** and **correlation ID** in the response.
- Ensures the error code in the response body is `0` (No Error).
- Includes at least one entry for the API key `18` (API_VERSIONS) with a `MaxVersion` of at least 4.

#### Handling Multiple Sequential Requests
- Modified the server to handle multiple sequential requests from the same client.
- Ensures that the server continues to listen for and process additional requests from the same connection.

#### Handling Concurrent Requests from Multiple Clients
- Added support for handling concurrent requests from multiple clients.
- Uses threads to handle each client connection independently, allowing the server to process requests from multiple clients concurrently.

## How to Run

To run the server, execute the following command:

```sh
$ ./your_program.sh
```

The server will start listening on port 9092 and will be ready to accept client connections.

## Testing

The server has been tested to handle `APIVersions` (v4) requests from multiple clients. Each response is validated to ensure:

- The first 4 bytes of the response (the "message length") are valid.
- The correlation ID in the response header matches the correlation ID in the request header.
- The error code in the response body is `0` (No Error).
- The response body contains at least one entry for the API key `18` (API_VERSIONS) with a `MaxVersion` of at least `4`.
