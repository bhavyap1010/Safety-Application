**General App Description and Layout:**

This app empowers users by helping them assess, plan, and manage their safety before, during, and after an abusive relationship. It reflects the reality that each situation is unique and plans must be tailored, flexible, and revisited regularly. This app never collects or stores identifiable information such as the user’s name, address, phone number, etc, unless the user permits (i.e. uploading documents). The user gets to control what is saved or deleted. It uses gentle, non-blaming language so users do not feel pressured. It is important to note that this app is not a substitute for emergency services but rather a tool to help users navigate their situation. 

**Features:**

- Login Screen: The first time the user seeks to use the app, they are prompted with a login screen. Here they have the option to login using an email and password or with their Google account.

- Pin Setup: After logging in for the first time, the user can set up a 4-digit pin that they can use to enter the app. On future attempts to access the app, the user will be asked to enter their 4-digit pin. They will also have the option to login with their email and password or Google account.

- Questionnaire: To ensure the user gets a tailored plan for their situation, the user is asked to complete a thorough questionnaire. The questions are tailored to the user's situation and can be changed and edited at any time. 

- Plan Generation: Based on the user's answers, the app will automatically generate a specialized plan for the user. In case the user decides to change their answers in the questionnaire, the plan will update to reflect the user's latest status.

- Support Resources: Based on the city chosen the user will have a page generated where they can access direct links to local victim services, hotlines, shelters, legal aid, and police for their city. 

- Storing Items: Users can store their emergency information by clicking the “items” button on the home screen. Here the user can upload documents to pack (IDs, court orders, etc.), emergency contacts (name, relationship, phone), safe locations (addresses, notes), and medications (name, dosage). Users can add, edit, delete items in each category.

- Reminders: Users can set reminders in the app which they can add, edit, and delete reminders so their safety plan is kept up to date.

- Exit Button: In case the user  needs to quickly exit the app, there is an exit button that the user can press that will redirect them to Google Chrome.


**Test Instructions:**

Note by following these steps you are not using the final published app, this is a local instance. Since there are multiple instances running, each instance has its own fingerprint and Google needs to know which instances to allow to sign in. By following these steps Google will recognize that you can use the sign in feature on your instance.

1. Clone this github repository to your local system using the following command:
   ```

      https://github.com/bhavyap1010/B07FinalProject
   ```

3. Run the command:
   ```

    ./gradlew signingReport
   ```

4. Locate the value that follows SHA1. Copy this key.
5. On Firebase go to:
      a. Firebase project<br>
      b. Project Settings<br>
      c. General<br>
      d. Your Apps<br>
      e. Add Fingerprint (paste here)<br>
6. Scroll to the top of Your Apps and click the download icon for google-services.json and place it in app/google-services.json.
7. Enable notifications in the phone settings for the app.
8. Run the app.


**Members Contributions:**


Hamed: Questionnaire<br>
Aryan:  Support resources page, Plan Generation Page<br>
Bhavya: Email/Password and Google Login, Setting Reminders, User Interface<br>
Tri: Add/Edit/Delete items<br>
Michael: Exit Button and Disclaimer Pop-Up<br>
Tony: Pin and Notification system, Realtime Database Security<br>
