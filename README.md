# SBPS API Service

Сервіс, що відповідає за бізнес-логіку, автентифікацію користувачів, збереження налаштувань та взаємодію з базою даних і фронтендом.

## Вимоги (Prerequisites)

* **Java 21** (JDK)
* **PostgreSQL** (Створена база даних `sbps_api`)

## Налаштування (Configuration)

Перед запуском переконайтеся, що налаштування в `src/main/resources/application.yml` коректні, або передайте змінні оточення:

* `DB_URL`, `DB_USER`, `DB_PASS`: Доступи до PostgreSQL (база `sbps_api`).
* `JWT_SECRET`: Секретний ключ для токенів (має збігатися з тим, що в `sbps-device`).
* `VAPID_PUBLIC_KEY`, `VAPID_PRIVATE_KEY`: Ключі для WebPush сповіщень.
* `DEVICE_URL`: URL сервісу пристроїв (за замовчуванням `http://localhost:8081`).

## Запуск (Running)

### Windows
```cmd
gradlew.bat bootRun
```

### Linux / macOS

```Bash
./gradlew bootRun
```

Сервіс запуститься на порту 8080.

## Тестування (Testing)

Запуск Unit та Integration тестів:

## Windows

```DOS
gradlew.bat test
```

## Linux / macOS

```Bash
./gradlew test
```