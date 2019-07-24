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

New strings are added constantly. To keep in sync, save a copy of the english version when you are done with your translation.
The next time you want to update your translation, compare the current english version with your previously saved english version with a diff tool of your choice.
This way you can immediatelly see what has changed since your last translation and adapt your translation accordingly.
Finally, submit a pull request of your updated file and save a new copy of the current english version for your next update.
