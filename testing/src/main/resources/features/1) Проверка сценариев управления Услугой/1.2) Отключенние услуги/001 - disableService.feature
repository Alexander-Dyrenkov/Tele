# language: ru
Функционал: Проверка успешного отключения услуги CNN

  Сценарий: Позитивный тест
    Дано msisdn = 79671863761, oldMsisdn = 79671863762, techMsisdn = 79671863763
    И Включение ошибки теста при ошибке в ответе веб-сервиса
    И Очистка таблиц
    И Устанавливаем дефолтные настройки приложения
    И Наполнение БД
    И Добавление белого номера и записи об отправленных SMS с целью проверки их удаления при отлючении сервиса
    И Отключение сервиса
    Когда Проверка, что статус отключен
    То Проверка технического номера
    И Проверка, что список белых номеров не пуст
    И Проверка, что список записей не пуст
    И Проверка сколько СМС было отправлено