// Auto-fill JWT token in Swagger UI (Development Only)
window.onload = function() {
    // Wait for Swagger UI to load
    setTimeout(() => {
        const defaultToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwaGF0bWVuZ2hvcjE5QGdtYWlsLmNvbSIsInJvbGVzIjoiUk9MRV9QTEFURk9STV9PV05FUiIsImlhdCI6MTc2NDgxOTA3OCwiZXhwIjoxMDAwMDE3NjQ4MTkwNzh9.WjekMwWCyYM3SGF2TRbkh5tF2ot87l6nNptqA8OyhNB1IphlHkR8Rhf4XoydpJQTtkN1bWP04xBmyIq8KMrexA";

        // Store token in localStorage for Swagger UI
        if (defaultToken) {
            localStorage.setItem('authorized', JSON.stringify({
                "bearerAuth": {
                    "name": "bearerAuth",
                    "schema": {
                        "type": "http",
                        "scheme": "bearer",
                        "bearerFormat": "JWT"
                    },
                    "value": defaultToken
                }
            }));
            console.log("✅ JWT token auto-loaded!");
        }
    }, 500);
};