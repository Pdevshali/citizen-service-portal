This folder contains example GitHub Actions workflows for CI/CD. The primary workflow is `ci-cd.yml` which builds backend and frontend, publishes Docker images, and optionally triggers Render and Vercel deploys.

Create the `ci-cd.yml` file by copying the template from the project README or using the provided template. Ensure you add the required secrets in your repo settings:
- DOCKERHUB_USERNAME
- DOCKERHUB_TOKEN
- RENDER_API_KEY (optional)
- VERCEL_TOKEN (optional)

Notes:
- The workflow builds multiple backend services and expects a Dockerfile in each service directory. Modify `services` array in the workflow to match your repo.
- For production, prefer Render auto-deploy or controlled deploy via the Render API.
