# language: ru
Функционал: Обработка звонка с отключенной опцией выборочного оповещения у Абонента А, у звонящего подключена услуга Анти-определитель номера

  Сценарий: Отключенная опция выборочного оповещения у абонента А (с анти определителем номера №1)
    Дано msisdn абонента А = "####", msisdn абонента Б = "79031111111", oldMsisdn = "79051111111", techMsisdn = "301"
    И Очистка таблиц
    И Устанавливаем дефолтные настройки приложения
    И Добавление технического номера, абонента с подключенной услугой и ответа заглушки для отправки СМС
    Когда Сделать дефолтный звонок
    То Промежуточная проверка переадресации на IVR
    И Проверка сколько СМС было отправлено
