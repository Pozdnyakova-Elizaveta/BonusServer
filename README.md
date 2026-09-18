# Бонусный сервер

## REST-сервис

Реализован REST - сервис для следующих сценариев:

1) Начислить бонусы по карте клиента
2) Списать бонусы по карте клиента
3) Возврат списанных/начисленных бонусов (этот кейс будет срабатывать, например, при возврате товара)
4) Получить баланс по номеру карты
5) Получить историю операций по номеру карты

## Миграции БД

Реализовано с помощью Liquibase

Скрипты:

- changelog-create-bonus-account.sql - создание таблицы бонусных счетов
- changelog-create-bonus-operation.sql - создание таблицы бонусных операций
- changelog-create-user.sql - создание таблицы пользователей
- changelog-create-index-bonus-operation.sql - создание индекса для таблицы бонусных операций

## Авторизация

Реализованы регистрация и аутентификация. Созданы две роли (на чтение и на запись), авторизация происходит через
JWT-токен, пароли хешируются через BCrypt.

Для авторизации полученный токен при аутентификации передается через заголовок в виде:

Authorization: Bearer eyJhbGciOiJ...

## Оптимизация

В PostgreSQL для первичных ключей и UNIQUE-атрибутов автоматически создается Btree-индекс, поэтому индексы уже
существовали:

- users - по полю id и по полю login
- bonus_account - по полю id и по полю card_number
- bonus_operation - по полю id

Дополнительно добавлен индекс (account_id, creation_at desc) в таблице bonus_operation для оптимизации получения истории
операций

## API Endpoints:

| Метод | URL                                    | Тело запроса / Параметры            | Описание                                   | Доступ                |
|-------|----------------------------------------|-------------------------------------|--------------------------------------------|-----------------------|
| POST  | `/bonus_server/accrual`                | JSON: cardNumber, amountBonus       | Начисление бонусов на карту                | ROLE_WRITE            
| POST  | `/bonus_server/deduction`              | JSON: cardNumber, amountBonus       | Списание бонусов с карты                   | ROLE_WRITE            
| POST  | `/bonus_server/cancel`                 | JSON: operationId                   | Отмена операции по id и возврат бонусов    | ROLE_WRITE            
| GET   | `/bonus_server/history?cardNumber=`    | Query: cardNumber, page, size, sort | Получение истории операций по номеру карты | ROLE_WRITE, ROLE_READ 
| GET   | `/bonus_server/balance?cardNumber=...` | Query: cardNumber                   | Получение баланса по номеру карты          | ROLE_WRITE, ROLE_READ 
| POST  | `/bonus_server/register`               | JSON: login, password, role         | Регистрация пользователя                   | Любой                 
| POST  | `/bonus_server/login`                  | JSON: login, password               | Аутентификация пользователя                | Любой                 

## Подготовка к запуску

Прописать в [application.properties](src/main/resources/application.properties) url-путь до БД, логин и пароль для
подключения

Подставить секретный ключ в [application.properties](src/main/resources/application.properties) jwt.secret

При необходимости изменить hibernate- и liquibase-параметры
