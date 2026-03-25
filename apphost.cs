#:sdk Aspire.AppHost.Sdk@13.2.0
#:package Aspire.Hosting.Java@13.2.0
#:package Aspire.Hosting.JavaScript@13.2.0
#:package Aspire.Hosting.Python@13.2.0

var builder = DistributedApplication.CreateBuilder(args);

// API — Spring Boot backend (Java/Maven)
var api = builder.AddSpringApp("api", "./src/api")
    .WithMavenBuild()
    .WithHttpHealthCheck("/health")
    .WithExternalHttpEndpoints();

// Web — Next.js frontend
builder.AddJavaScriptApp("web", "./src/web")
    .WithRunScript("dev")
    .WithNpm(installCommand: "ci")
    .WithEnvironment("NEXT_PUBLIC_API_URL", api.GetEndpoint("http"))
    .WithReference(api)
    .WaitFor(api)
    .WithHttpEndpoint(env: "PORT")
    .WithExternalHttpEndpoints()
    .PublishAsDockerFile();

// Docs — MkDocs documentation server
builder.AddPythonExecutable("docs", "specs", "mkdocs")
    .WithArgs("serve", "--dev-addr", "0.0.0.0:8000")
    .WithHttpEndpoint(port: 8000)
    .WithExternalHttpEndpoints();

builder.Build().Run();
