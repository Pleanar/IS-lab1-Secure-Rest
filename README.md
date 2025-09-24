# Информационная безопасность

Этот репозиторий содержит лабораторную работу №1:  
**"Разработка защищенного REST API с интеграцией в CI/CD"**

## Secure REST API

### Описание

Простое REST API приложение с базовой аутентификацией на основе JWT и защитой от XSS и SQL-инъекции.

---

### Использованные технологии

- Spring Security  
- Spring Data + PostgreSQL  
- JJWT (Java JWT библиотека)  
- OWASP Dependency-Check + SpotBugs

---

### Реализованные эндпоинты

#### Доступные всем

- `POST /auth/registration` — регистрация нового пользователя.  
  Пример запроса:
{
"username": "Dummy",
"password": "DummyPass"
}

- `POST /auth/login` — аутентификация пользователя, получение JWT.  
Пример запроса:
{
"username": "Dummy",
"password": "DummyPass"
}

- `POST /auth/refresh_token` — обновление JWT.  
Заголовок: Authorization: Bearer <Your.JWT_REFRESH_TOKEN.Here>

#### Доступные авторизованным пользователям

- `DELETE /auth/user?username=<username_here>` — удаление пользователя с именем `<username_here>`.  
Доступно администраторам и пользователю с данным именем.  
Заголовок: Authorization: Bearer <Your.JWT_ACCESS_TOKEN.Here>

- `GET /api/data` — получение информации о зарегистрированных пользователях.  
Администраторы видят расширенную информацию.  
Заголовок: Authorization: Bearer <Your.JWT_ACCESS_TOKEN.Here>

---

## Меры безопасности

### SQL-инъекции

Доступ к базе данных реализован через Spring Data, который использует подготовленные запросы, исключающие SQL-инъекции.

### XSS-защита

Реализован фильтр `XssFilter`, очищающий все входящие параметры и заголовки HTTP-запроса с использованием OWASP Encoder. Это предотвращает внедрение скриптов.

### JWT аутентификация

Используется библиотека JJWT с фильтром `JwtFilter`, который проверяет JWT в каждом запросе к защищённым эндпоинтам. Без валидного токена доступ запрещён.

### Валидация данных

Входные данные проверяются с помощью Hibernate Validator (аннотации `@NotBlank`, `@Size`, `@Pattern`), что предотвращает внедрение вредоносных данных и гарантирует корректный формат.

---

## Интеграция анализа безопасности

### SAST (Static Application Security Testing)

Используется SpotBugs для статического анализа кода. Запускается автоматически в CI/CD при push и pull-request. Исправлены предупреждения, участки с ложными срабатываниями разобраны.

<img width="1396" height="367" alt="SpotBugs" src="https://github.com/user-attachments/assets/682a997f-21b8-4057-945a-235884a8a26b" />

Большинство проблем, указанных на картинке связаны с тем, что на вход классам подаются Mutable объекты, однако поскольку это всё управляется Spring, то значительных проблем с этим нет.
Единственное, что в классе User не было помечено поле аннотацией @Transient, т.к. оно существовало чисто для указания отношения между Entity: User-OneToMany-Token.

### SCA (Software Composition Analysis)

Используется OWASP Dependency-Check для анализа безопасности зависимостей. Также интегрирован в pipeline. Используется зеркало Known Exploited Vulnerabilities (KEV) с GitHub для стабильности обновлений.

<img width="1920" height="912" alt="OWASP-1" src="https://github.com/user-attachments/assets/7087ce2b-4139-4f0c-aa29-7b177b3840cf" />

Обнаружено две уязвимости, связанные с Spring Security и использованием аннотаций.

<img width="1920" height="894" alt="OWASP-1 1" src="https://github.com/user-attachments/assets/83119d4e-cda5-4838-a241-623011ad7abf" />
<img width="1920" height="856" alt="OWASP-1 2" src="https://github.com/user-attachments/assets/cf233668-26dc-41fc-bb86-57ef4bf7e42b" />

После замены соответсвующей аннотации @PreAuthorize и @EnableMethodSecurity на логическое соответсвие уязвимость была убрана в suppression-файл.

<img width="1920" height="863" alt="OWASP2" src="https://github.com/user-attachments/assets/e9d7d801-3f72-43d1-8a27-37c0752bb7e6" />

---

## Тестирование

API тестировалось с помощью :  

- Intelij Idea HTTP-requests.
- SpotBugs - анализ приложения.
- OWASP - анализ зависимостей.

---

## Ссылки

- Репозиторий: [https://github.com/Pleanar/IS-lab1-Secure-Rest]  
- Последний успешный запуск CI/CD pipeline: [https://github.com/Pleanar/IS-lab1-Secure-Rest/actions/runs/17974902627/job/51126004339]  
- Примеры запросов — в директории `/docs`
