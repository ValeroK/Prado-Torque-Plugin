# Project OverView
1. Create an Android application that is aligned with the latest Android API and standards
2. This application is a plugin that is used to import vehicle PIDs into Torque Pro Android app using the standard Torque Pro plugin API
3. The app will be able to import PIDs from a local file or from GitHub and will be able to update PIDs from GitHub
4. the design of the app will be in the latest material design and will be able to be used with android device 8+
5. The app will use latest gradle, kotlin and java versions and standards

# Core Functionalities
2. The app will connect to Torque and verify that the app is connected to the right version of Torque using ITorqueService.aidl
3. The app will download from GitHub all relevant zip files that contain a CSV with the relevant PIDs
4. The user will select the PIDs they want to import
5. The app will import the PIDs into the Torque app using ITorqueService.aidl
6. The app will be able to import PIDs from a local CSV file
7. The app will also support importing multiple CSV files

# Doc // Reference doc used above

