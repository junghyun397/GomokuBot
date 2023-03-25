#!/bin/zsh

TARGET_LANG="Japanese"
PROMPT="The class below is the English Language Container for the chatbot I'm developing. Please translate it into $TARGET_LANG and create a $TARGET_LANG Language Container."

cp core/src/main/kotlin/core/interact/i18n/LanguageENG.kt temp.kt
sed -i "s;    // chunk;}\n$PROMPT\n\nopen class LanguageENG : LanguageContainer() {;g" temp.kt
