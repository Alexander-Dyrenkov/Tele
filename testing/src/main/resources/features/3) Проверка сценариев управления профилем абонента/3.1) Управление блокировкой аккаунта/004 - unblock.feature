# language: ru
Функционал: Управление блокировкой аккаунта

  Сценарий: Разблокировка звонков
    Дано msisdn "79671863761", oldMsisdn = "79671863762"
    И Очистка таблиц
    И Заполнение базы с учетов абонента с типом заблокировать все
    И Устанавливаем дефолтные настройки приложения
    Когда Изменение параметров аккаунта с пустыми значениями
    То Сервис является актуальным
    И Проверка, что разблокировка сработала успешно
