# Prado Torque Plugin Development Instructions

## Latest Changes (2025-01-14)

### Service Binding Implementation - MILESTONE ACHIEVED
Successfully implemented service binding with Torque Pro app. The key components are:

1. Required Permissions:
   - Storage permissions: `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`
   - Internet permissions: `INTERNET`, `ACCESS_NETWORK_STATE`
   - Basic Torque permission: `org.prowl.torque.permission.HANDSHAKE`

2. Service Connection Components:
   - `TorqueServiceManager`: Handles service binding lifecycle
   - `PermissionManager`: Manages storage permissions
   - Service binding checks in activities

3. Key Implementation Details:
   - Service binding through explicit intent to "org.prowl.torque.remote.TorqueService"
   - Verify Torque installation before binding
   - Handle connection callbacks properly
   - Proper error handling and user feedback

## Next Steps

### PID Import Implementation
1. Review available PID import methods in ITorqueService.aidl:
   - `sendPIDData`: Basic PID data sending (deprecated)
   - `sendPIDDataPrivate`: Private version of PID data sending
   - `sendPIDDataV2`: Latest version with extended functionality
   
2. Choose appropriate method based on:
   - API version compatibility
   - Required data fields
   - Deprecation status
   - Documentation completeness

3. Implementation Tasks:
   - Select the most suitable PID import method
   - Format PID data according to method requirements
   - Implement error handling
   - Add user feedback for import status

### Testing Requirements
1. Test PID Import:
   - Verify data format
   - Check successful import in Torque
   - Handle error cases
   - Test with different PID formats

## Current Status
- Service binding successfully implemented
- Next phase: PID import implementation
- Working on selecting the appropriate PID import method from ITorqueService.aidl

## Known Issues
1. Need to verify correct PID import method selection
2. Need to test PID data format compatibility with Torque

## Resources
- [Torque Plugin Documentation](https://torque-bhp.com/wiki/Torque_Plugin_Documentation)
- [Android Service Binding Guide](https://developer.android.com/guide/components/bound-services)
- [Android IPC Documentation](https://developer.android.com/guide/components/aidl)

## Progress Tracking
- [x] Basic plugin structure
- [x] Permission configuration
- [x] Service manager implementation
- [x] Successful service binding
- [ ] Data communication with Torque
- [ ] PID management implementation

## Notes
- Keep testing with different Android versions
- Monitor logcat output for binding issues
- Consider reaching out to Torque community for guidance
- May need to analyze working plugin implementations

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

# Prado Torque Plugin Development Progress

## Session Updates (2025-01-08)

### Service Binding Approach Evolution

#### Initial Broadcast Receiver Approach
- **Attempted**: Implemented a broadcast receiver pattern for service binding
- **Status**: Abandoned due to complexity and potential reliability issues
- **Learning**: While broadcast receivers can work, direct service binding is more reliable for our use case

#### Direct Service Binding Implementation
- **Successful Changes**:
  - Implemented direct service binding using `ComponentName`
  - Added proper permission handling with `ContextCompat`
  - Improved error handling and logging throughout binding process
  - Separated permission and connection listeners for better separation of concerns

#### Key Components Modified

1. **TorqueServiceManager.java**:
   - Added robust context handling using application context
   - Implemented comprehensive error checking for PackageManager
   - Added detailed logging for debugging
   - Created separate interfaces for connection and permission callbacks
   - Improved error message clarity and consistency

2. **PidImportActivity.java**:
   - Updated to implement both connection and permission interfaces
   - Improved permission handling UI flow
   - Added proper error handling and user feedback

3. **AndroidManifest.xml**:
   - Added required Torque plugin permission:
     ```xml
     <uses-permission android:name="org.prowl.torque.permission.PLUGIN" />
     ```

### Challenges and Solutions

1. **PackageManager Initialization**
   - **Challenge**: Unreliable PackageManager initialization
   - **Solution**: Added multiple validation layers:
     - Context null checks
     - PackageManager initialization verification
     - Comprehensive exception handling
     - Detailed logging for debugging

2. **Permission Handling**
   - **Challenge**: Complex permission flow
   - **Solution**: 
     - Separated permission logic into dedicated interface
     - Added clear user feedback through dialog
     - Implemented proper permission checking with ContextCompat

3. **Service Connection**
   - **Challenge**: Unreliable service binding
   - **Solution**:
     - Direct ComponentName-based binding
     - Proper connection state management
     - Comprehensive error handling
     - Clear user feedback

### Best Practices Implemented

1. **Context Management**
   - Using ApplicationContext to prevent memory leaks
   - Proper null checking and validation
   - Clear error messages for context-related issues

2. **Error Handling**
   - Comprehensive try-catch blocks
   - Detailed logging at each step
   - User-friendly error messages
   - Proper error propagation to UI

3. **Code Organization**
   - Clear separation of concerns
   - Interface-based communication
   - Consistent logging patterns
   - Clean code structure

### Next Steps

1. **Testing**
   - Implement comprehensive testing for service binding
   - Test permission flows on different Android versions
   - Verify error handling in various scenarios

2. **UI Improvements**
   - Enhance permission request dialog
   - Add progress indicators during binding
   - Improve error message presentation

3. **Documentation**
   - Add detailed comments for complex operations
   - Document error handling patterns
   - Create user guide for permission handling

### Known Issues

1. **Service Binding**
   - May need additional retry logic for unstable connections
   - Could benefit from timeout handling

2. **Permissions**
   - May need to handle Android 13+ permission changes
   - Could add more detailed permission explanation dialogs

### Testing Requirements

1. Test service binding on:
   - Different Android versions
   - Various device manufacturers
   - Different Torque Pro versions

2. Verify permission handling:
   - First-time permission requests
   - Permission denials
   - Permission revocation scenarios

### References

- [Android Service Documentation](https://developer.android.com/guide/components/services)
- [Android Permissions Best Practices](https://developer.android.com/training/permissions/requesting)
- Torque Pro API Documentation
