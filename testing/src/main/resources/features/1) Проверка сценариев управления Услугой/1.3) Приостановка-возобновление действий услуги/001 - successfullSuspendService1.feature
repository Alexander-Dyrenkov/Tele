# language: ru
Функционал: Проверка успешной приостановки действия услуги CNN

  Сценарий: Позитивный тест
    Дано msisdn = 79030000001, oldMsisdn = 79030000002, techMsisdn = 301
    И Очистка таблиц
    И Устанавливаем дефолтные настройки приложения
    И Включение ошибки теста при ошибке в ответе веб-сервиса
    И Добавление течнического номера и ответа заглушки для отправки СМС
    Когда Отключение сервиса
    То Проверка, что статус отключен
    И Проверка сколько СМС было отправлено