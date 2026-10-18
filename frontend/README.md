# Shopnest frontend

React storefront for the Spring Boot API in `backend/backend`.

## Run locally

1. Use Node.js `20.19+` or `22.12+`.
2. Start the Spring Boot backend on `http://localhost:8080`.
3. From this directory, install dependencies and start Vite:

   ```sh
   npm install
   npm run dev
   ```

4. Open the Vite URL shown in the terminal (normally `http://localhost:5173`).

Vite proxies `/api` requests to `http://localhost:8080`, so the session cookie and API requests use the same browser origin during development.

The backend currently has no endpoint to look up the signed-in customer profile. The frontend therefore creates a delivery profile after first sign-in and stores its customer ID in this browser. The backend validates ownership when the customer ID is used.
