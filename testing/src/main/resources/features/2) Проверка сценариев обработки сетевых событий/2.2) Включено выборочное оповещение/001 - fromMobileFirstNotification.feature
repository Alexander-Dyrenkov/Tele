# language: ru
Функционал: Обработка звонка с мобильного телефона с включенной опцией выборочного оповещения у Абонента А

  Сценарий: первое уведомление после включения опции выборочного уведомления
    Дано msisdn абонента А = "79032222222", msisdn абонента Б = "79031111111", oldMsisdn = "79051111111", techMsisdn = "79051111166", notificationAllowedSms = "2"
    И Очистка таблиц
    И Устанавливаем дефолтные настройки приложения
    И Добавление течнического номера и ответа заглушки для отправки СМС с опцией выборочного уведомления
    Когда Сгенерировать звонок
    То Дождаться соединения
    И Проверка сколько СМС было отправлено
    И Проверка записи контроля СМС
    И Проверка актуального сервиса
    Когда Сгенерировать отправку смс
    То Проверка белого номера
    И Проверка записи контроля СМС
    И Проверка сколько СМС было отправлено
    Когда Очистить прогресс
    То Сгенерировать звонок
    И Проверка записи
    И Проверка сколько СМС было отправлено
