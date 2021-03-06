# language: ru
Функционал: Обработка звонка с мобильного телефона с отключенной опцией выборочного оповещения у Абонента А

  Сценарий: Отключенная опция выборочного оповещения у абонента А
    Дано msisdn абонента А = "79032222222", msisdn абонента Б = "79031111111", oldMsisdn = "79051111111", techMsisdn = "301"
    И Очистка таблиц
    И Устанавливаем дефолтные настройки приложения
    И Добавление технического номера, абонента с подключенной услугой и ответа заглушки для отправки СМС
    Когда Сделать дефолтный звонок
    То Дождаться соединения
    И Проверка сколько СМС было отправлено
