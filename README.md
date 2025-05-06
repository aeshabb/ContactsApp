# Приложение для просмотра контактов с возможностью удалить повторяющиеся.

## Запуск приложения
Для запуска в Android Studio можно использовать:
```
git clone https://github.com/aeshabb/ContactsApp.git
```

Также можно собрать apk, без подписи ключем:
```
./gradlew assembleRelease
```
Найти можно по пути: `app/build/outputs/apk/release/app-release-unsigned.apk`


Или же с подписью, потребуется утилита keytool. Создаем ключ, помещаем в папку keystore, в корне проекта.
```
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
```

Далее в разделе `android` в файле `build.gragle.kts` модуля `app` прописываем:
```
android {
    // ...

    signingConfigs {
        create("release") {
            storeFile = file("../keystore/my-release-key.jks")  // путь к файлу ключа (относительно модуля)
            storePassword = "your_keystore_password"  // пароль от хранилища (хардкод или выносим в properties)
            keyAlias = "my-alias"  // алиас ключа
            keyPassword = "your_key_password"  // пароль ключа (хардкод или выносим в properties)
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

Для сборки та же команда:
```
./gradlew assembleRelease
```
Найти можно по пути: `app/build/outputs/apk/release/app-release.apk`

**Важно** 
При сборке apk в терминале linux на Java версий старше 17 могут возникать конфликты из-за библиотек Android и Kotlin. Поэтому для сборки может понадобиться 17 JDK.

## Тесты
Приложение тестровалось на устройствах Google Pixel 8 pro, Small Phone на базе Android 16.0 и API 36 в Android Studio. А также локально на устройствах Redmi Note 8 Pro и POCO X3 PRO на Android 11.0.
Программных тестов нет, таких как Unit и т.д.

## Возможности приложения
В ходе работы над приложением было устранено множество багов. Например, при создании одного контакта с несколькими одинаковыми номерами данный контакт расенивался как 2, это было исправлено, теперь только каждый уникальный номер контакта отображается в списке. Контакты, которые хранятся на SIM или SD карте не учитываются в списке и не удаляются, в соответствии с заданием. Все контакты отсортированы лексикографически по имени и находятся в блоках с первым символом имени. После удаления пользователю сообщается информация, сколько контактов было удалено или же не было удалено, после чего происходит обновление списка. Также при отсутствии контактов отображается, что контактов нет.
