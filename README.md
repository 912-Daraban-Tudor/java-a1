# Servlet Assignment

**Author:** Tudor Daraban
**Group:** MaSDI

---

## 1. Introduction 

The primary objective of this project is to implement and configure a distributed Java application. The core requirement is to create a system where a minimum of two servlets, deployed in separate and distinct servlet containers, can communicate with each other. This simulates a basic **microservice architecture**, where discrete services handle specific tasks.

The project demonstrates:
1.  Installation and simultaneous configuration of three major application servers: **Apache Tomcat, Jetty, and WildFly**.
2.  The creation of two separate web applications (`.war` files).
3.  Cross-container communication, where a "frontend" servlet makes an **HTTP request** to a "backend" servlet.
4.  A functional **UI** for user interaction.
5.  Manual deployment of applications outside of an IDE.

---

## 2. System Architecture

The solution consists of two independent web applications: `frontend-app` and `backend-app`.

* **`frontend-app` (The Client):** Deployed on **Apache Tomcat (port 8080)**. It contains the user-facing HTML/CSS/JS files and a servlet (`ServletA`) that acts as a controller and HTTP client.
* **`backend-app` (The Service):** Deployed on **Jetty (port 9090)**. It contains only a single servlet (`ServletB`) that acts as a worker, performing business logic.

### 2.1. Data Flow

The sequence of operations is as follows:

1.  A user opens `http://localhost:8080/frontend-app/` in their browser. Tomcat serves the `index.html` file.
2.  The user fills out the form and clicks "Process".
3.  The JavaScript on the page captures this event and sends an **AJAX (Fetch) POST request** to `ServletA`, which is running on the same Tomcat server.
4.  `ServletA` receives the request. It then uses the `java.net.http.HttpClient` to create a *new* **POST request**, packaging the user's data.
5.  `ServletA` sends this new request across the network to `ServletB` at `http://localhost:9090/backend-app/servletB`.
6.  Jetty receives the request and routes it to `ServletB`.
7.  `ServletB` performs the requested string operation (e.g., "reverse") and writes the plain-text result (e.g., "olleH") to its response.
8.  `ServletA` receives the "olleH" text as a response from `ServletB`.
9.  `ServletA` writes this same text as its *own* response back to the user's browser.
10. The JavaScript in the browser receives "olleH" and displays it in the "Result" box.

### 2.2. The CORS Problem

Because the UI (on `localhost:8080`) is making a call to a resource on a different port (`localhost:9090`), browser security blocks the request. This is known as a **Cross-Origin Resource Sharing (CORS)** violation.

To solve this, `ServletB` (the backend) must explicitly send **CORS headers** with its response. This tells the browser that it trusts the origin `http://localhost:8080` and permits the communication.

---

## 3. Component & Object Description

This project uses two main Java classes. The source code is not included, as per the instructions.

### 3.1. Project: `frontend-app`

* **`ServletA.java`**
    * **Description:** This servlet acts as the frontend controller and as a client to the backend service. It is mapped to the URL `/servletA` using the `@WebServlet` annotation.
    * **Methods:**
        * `protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException`
            * **Parameters:** `request` (contains the form data from the UI) and `response` (used to send the final result back to the UI).
            * **Functionality:** Extracts `inputText` and `operation` from the `request`. It builds and sends an HTTP POST request to `ServletB` using `java.net.http.HttpClient.send()`. It then forwards the response (or error message) from `ServletB` back to the browser.
            * **Exceptions Thrown:** `IOException` (if the connection to `ServletB` fails), `InterruptedException` (from the `HttpClient`).

### 3.2. Project: `backend-app`

* **`ServletB.java`**
    * **Description:** This servlet acts as the backend worker. It is defined in `web.xml` to handle all requests for the URL pattern `/servletB`.
    * **Methods:**
        * `protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException`
            * **Parameters:** `request` (contains the data from `ServletA`) and `response` (used to send the processed result back to `ServletA`).
            * **Functionality:** First, it sets the `Access-Control-Allow-Origin` header on the `response` to permit the cross-origin request. It then extracts the `inputText` and `operation` parameters. A `switch` statement performs the required business logic (reverse, uppercase, or count). The final string result is written to the `response.getWriter()`.
        * `protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException`
            * **Functionality:** Handles the browser's "preflight" `OPTIONS` request. This method *only* sets the required CORS headers (e.g., `Access-Control-Allow-Methods`) to inform the browser that `POST` requests are permitted.

---

## 4. Operational Context & Deployment

* **Build Tool:** Apache Maven
* **Java Version:** OpenJDK 17
* **Operating System:** CachyOS
* **Application Servers:**
    * **Tomcat:** `Apache Tomcat 10.1.49` (Port: `8080`)
    * **Jetty:** `Jetty 12.1.3` (Port: `9090`), from jetty-base dir
    * **WildFly:** `WildFly 36.0.0.Final` (Port: `7070`)

**Deployment (from terminal, not IDE):**
1.  All three servers (or at least Tomcat and Jetty) are started in separate terminals, on their configured ports.
2.  `mvn package` is run in both `frontend-app` and `backend-app` directories to build their respective `.war` files.
3.  `cp frontend-app/target/frontend-app.war ~/servers/apache-tomcat-10.1.49/webapps/`
4.  `cp backend-app/target/backend-app.war ~/servers/jetty-base/webapps/`
5.  The servers automatically detect and deploy the applications.

---

## 5. Usage Examples & Testing

The application's functionality was validated with the following test cases:

* **Test Case 1: "Reverse String"**
    * **Context:** User navigates to `http://localhost:8080/frontend-app/`.
    * **Input:** Text = "Java Servlet", Operation = "Reverse String"
    * **Expected Output:** The "Result" box updates to display "telvreS avaJ".
    * **Result:** **Success.**

* **Test Case 2: "Uppercase"**
    * **Context:** Same as above.
    * **Input:** Text = "hello world", Operation = "To Uppercase"
    * **Expected Output:** The "Result" box updates to display "HELLO WORLD".
    * **Result:** **Success.**

* **Test Case 3: "Backend Service Failure"**
    * **Context:** The Jetty server (port 9090) is manually stopped.
    * **Input:** Text = "test", Operation = "Reverse String"
    * **Expected Output:** The "Result" box displays an error: "An error occurred: HTTP error! Status: Service Unavailable" (or a similar network error).
    * **Result:** **Success.** The frontend correctly handles the backend failure.

---

## 6. Alternative Solutions

While direct HTTP communication was chosen for its simplicity, other patterns could have been used:

* **Message Queue:** `ServletA` could have published a message to a queue. `ServletB` would subscribe to that queue, process the message, and publish the result to a "reply" queue. This is an **asynchronous** pattern and is far more resilient to network failures but adds the complexity of a message broker.
* **Full REST Framework:** Instead of using raw servlets, a framework like Spring Boot could be used. This would abstract away the `doPost` methods into `@RestController` annotations and automatically handle JSON serialization/deserialization. This is the modern industry standard but would hide the low-level servlet mechanisms this project was designed to explore.
