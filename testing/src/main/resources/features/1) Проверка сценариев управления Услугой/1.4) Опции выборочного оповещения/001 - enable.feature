# language: ru
Функционал: Проверка включения выборочного оповещения о смене номера

  Сценарий: Позитивный тест
    Дано msisdn = 79671863761, oldMsisdn = 79671863762
    И Включение ошибки теста при ошибке в ответе веб-сервиса
    И Очистка таблиц
    И Устанавливаем дефолтные настройки приложения
    И Наполнение БД
    И Отключение сервиса
    Когда Проверка актуального сервиса
    То Проверка сколько СМС было отправлено
    И Проверка, что выборочное оповещение включено

