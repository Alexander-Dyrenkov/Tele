# language: ru
Функционал: Обработка звонка с отключенной опцией выборочного оповещения у Абонента А

  Сценарий: Абонент А заблокирован
    Дано msisdn абонента A = "79032222222", msisdn абонента Б = "79031111111", oldMsisdnB = "79051111111", techMsisdnB = "79051111166"
    И Очистка таблиц
    И Устанавливаем дефолтные настройки приложения
    И Добавление течнического номера и ответа заглушки для отправки СМС с учетом старого и нового номера абонента Б и сервис заблокирован
    Когда Сгенерировать звонок
    То Дождаться соединения
    И Проверка, что СМС не было отправлено
