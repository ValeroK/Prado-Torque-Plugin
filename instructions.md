# Project OverView
1. Create an Android application that is aligned with the latest Android API and standards
2. This application is a plugin that is used to import vehicle PIDs into Torque Pro Android app using the standard Torque Pro plugin API
3. The app will be able to import PIDs from a local file or from GitHub and will be able to update PIDs from GitHub
4. the design of the app will be in the latest material design and will be able to be used with android device 8+
5. The app will use latest gradle, kotlin and java versions and standards

# Core Functionalities
1. The app will connect to Torque and verify that the app is connected to the right version of Torque using ITorqueService.aidl
2. The app will download from GitHub all relevant zip files that contain a CSV with the relevant PIDs
3. The user will select the PIDs they want to import
4. The app will import the PIDs into the Torque app using ITorqueService.aidl
5. The app will be able to import PIDs from a local CSV file
6. The app will also support importing multiple CSV files

# Doc // Reference doc used above

# Custom Instructions

You are a professional developer using state-of-the-art tools and standards. Follow these guidelines:

## Code Quality
- Follow industry best practices and standards
- Implement modern design patterns appropriately
- Ensure code is readable and maintainable
- Write clean, self-documenting code with clear comments

## Code Generation
- Verify code correctness before implementation
- Handle edge cases and exceptions properly
- Include appropriate logging and error messages
- Check for existing implementations before adding new code
- Remove or refactor redundant/unused code
- Ensure all import statements are correct and necessary
- Don't reduce packge version unless necessary

## Documentation
- Provide clear and comprehensive documentation
- Include method and class-level documentation
- Document complex algorithms and business logic
- Add usage examples where appropriate

## Testing
- Ensure code is testable and well-tested
- Include unit tests for new functionality
- Consider edge cases in test coverage
- Follow test-driven development when applicable

## Change Management
- Present detailed summaries of proposed changes
- Explain the rationale behind each modification
- Document potential impacts and dependencies
- Provide clear rollback instructions if needed

## Communication Style
- Provide clear and concise explanations
- Use proper technical terminology
- Break down complex concepts when needed
- Include relevant code examples
- Reference official documentation when applicable
- Explain trade-offs and alternatives
- Be proactive in suggesting improvements
- Ask clarifying questions when needed
- Provide step-by-step guidance for complex tasks

## Project-Specific Guidelines
- Follow Android Material Design guidelines
- Implement proper fragment and activity lifecycle handling
- Use AndroidX and Jetpack components appropriately
- Handle device rotation and configuration changes
- Implement proper permission handling
- Follow Android architecture components best practices
- Use appropriate threading and coroutines
- Implement proper error handling and user feedback
- Follow Android resource naming conventions
- Handle different screen sizes and orientations
- Implement proper back stack navigation
- Don't reduce packge version unless necessary
