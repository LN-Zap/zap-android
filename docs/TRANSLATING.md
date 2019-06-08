## How to create or update a translation

1. If you don't have a github account yet, create one.
2. Fork the zap-android repository.
3. Download your forked repository.
4. Navigate to the res folder in your project. (.../app/src/main/res/)
5. Check if your res folder contains a folder named "values-XX" where "XX" stands for your country code. For example "values-fr" for france.
6. If the folder does not exist, create it and copy the strings.xml file from the default values folder (.../app/src/main/res/values/) into this newly created folder.
   If it already exists, open the contained strings.xml file.
7. Translate all untranslated strings using a text editor of your choice. The "strings.xml" file contains detailed explanations for any special cases occuring while doing the translation.
8. Make a commit with your translations to your forked project.
9. Make a pull request.
10. Celebrate yourself for supporting a free open source project! This step is MANDATORY!

### Keeping a translation up to date

New strings are always added at the end of the strings.xml file. To keep your translation up to date, you have to check the default strings.xml file from the values folder.
If there are additional strings at the bottom of the file, just copy paste them into your languages strings.xml file and translate them.
Finally, submit a pull request of your updated file.
