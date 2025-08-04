# GitHub Actions Workflows

This directory contains GitHub Actions workflows for the Fluent i18n project.

## Workflows

### 1. CI (`ci.yml`)
**Triggers**: Push to main/develop, Pull requests to main
**Purpose**: Build and test the project

- Builds the project with Maven
- Runs unit tests
- Runs integration tests
- Builds example applications
- Uploads test results as artifacts

### 2. I18n Process Test (`i18n-process.yml`)
**Triggers**: Push to main/develop, Pull requests to main, Manual dispatch
**Purpose**: Test the Fluent i18n extraction and compilation process

- Builds the Fluent i18n library
- Tests message extraction from example code
- Tests PO file compilation
- Verifies generated translation files
- Uploads generated files as artifacts

### 3. Maven Package (`maven-publish.yml`)
**Triggers**: Release creation, Tag push (v*)
**Purpose**: Publish artifacts to GitHub Packages

- Builds the project
- Runs tests
- Publishes to GitHub Packages
- Requires proper authentication via `settings.xml`

## Configuration Files

### `settings.xml`
Maven settings file for GitHub Packages authentication. Contains:
- Server configuration for GitHub Packages
- Repository configuration
- Authentication using GitHub tokens

### Required Environment Variables
- `GITHUB_TOKEN`: Automatically provided by GitHub Actions
- `GITHUB_ACTOR`: GitHub username
- `GITHUB_REPOSITORY`: Repository name (owner/repo)

## Publishing Process

1. **Create a Release**: Go to GitHub Releases and create a new release
2. **Tag the Release**: Use semantic versioning (e.g., v1.0.0)
3. **Workflow Triggers**: The maven-publish workflow will automatically run
4. **Artifacts Published**: All modules will be published to GitHub Packages

## Manual Testing

To test the workflows manually:

1. **CI Workflow**: Push to main or create a PR
2. **I18n Process**: Use the "workflow_dispatch" trigger in the Actions tab
3. **Publishing**: Create a release or push a tag

## Troubleshooting

### Common Issues

1. **Authentication Errors**: Ensure `settings.xml` is properly configured
2. **Build Failures**: Check Java version compatibility (JDK 21)
3. **Test Failures**: Review test results in artifacts
4. **Publishing Issues**: Verify repository permissions and token access

### Debug Steps

1. Check workflow logs in the Actions tab
2. Verify Maven configuration
3. Test locally with `mvn clean install`
4. Check GitHub Packages permissions

## Security

- All workflows use GitHub's built-in security features
- Tokens are automatically rotated
- No secrets are stored in plain text
- Repository permissions are properly scoped

## Contributing

When adding new workflows:
1. Follow the existing naming conventions
2. Include proper error handling
3. Add appropriate triggers
4. Test thoroughly before merging 