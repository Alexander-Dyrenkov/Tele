# language: ru
Функционал: Обработка звонка с отключенной опцией выборочного оповещения у Абонента А (для всех)

  Сценарий: Абонент А заблокирован
    Дано msisdn абонента A = "79032222222", msisdn абонента B = "79031111111", oldMsisdnB = "79051111111", techMsisdnB = "79051111166"
    И Очистка таблиц
    И Устанавливаем дефолтные настройки приложения
    И Добавление течнического номера и ответа заглушки для отправки СМС с учетом блокировки для всех
    Когда Сгенерировать звонок
    То Дождаться соединения
    И Проверка, что СМС не было оптравлено
