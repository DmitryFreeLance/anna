# Anna Bot

Telegram-бот на Java для продажи подписки в закрытый чат через ЮKassa.

Что умеет:

- `Long polling` для Telegram: бот не требует Telegram webhook.
- `/start` открывает главное меню.
- `/vip` отправляет текст `🤍 Выберите желаемый для вас тарифный план:` и inline-кнопки тарифов.
- `/subscription` показывает срок текущей подписки.
- Оплата через ЮKassa по API с webhook-обработкой `payment.succeeded` / `payment.canceled`.
- Отправка чеков через объект `receipt` в запросе на создание платежа.
- Автоматическая выдача одноразовой invite-ссылки в закрытый чат после оплаты.
- Напоминание за 3 дня до конца подписки.
- Удаление пользователя из закрытого чата после окончания подписки.
- Reply-клавиатура: `📚 Перейти к гайдам и тарифу`, `💎 Подписка`, `💬 Обратная связь`.
- Админ-панель `/admin`: изменение цены тарифов, статистика, список оплативших, список всех пользователей, массовая рассылка.

## Важные детали

- Для чеков ЮKassa нужен email, поэтому бот перед первой оплатой просит адрес.
- Бот реагирует только в личке. В самом закрытом чате он ничего не пишет и не обрабатывает сообщения.
- Пользователи, которые уже были в чате до внедрения бота и не проходили через подписку, не затрагиваются: удаляются только участники с `subscriptionManaged=true`.
- У reply-кнопки нельзя напрямую открыть чат с username. Поэтому при нажатии `💬 Обратная связь` бот отправляет кнопку-ссылку на `@AreninaAnna`.

## Переменные окружения

Смотри пример в [.env.example](/Users/dmitry/Desktop/anna/.env.example).

Минимально нужны:

- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_BOT_USERNAME`
- `TELEGRAM_PRIVATE_CHAT_ID`
- `TELEGRAM_ADMIN_IDS`
- `YOOKASSA_SHOP_ID`
- `YOOKASSA_SECRET_KEY`
- `YOOKASSA_RETURN_URL`

## Локальный запуск

```bash
./mvnw spring-boot:run
```

По умолчанию база H2 хранится в `./data/anna-bot`.

## Сборка jar

```bash
./mvnw package
```

## Docker

```bash
docker build -t anna-bot .
docker rm -f anna-bot 2>/dev/null || true

docker run -d \
  --name anna-bot \
  --restart unless-stopped \
  --network host \
  --add-host api.telegram.org:149.154.167.220 \
  -e SERVER_PORT='8080' \
  -e TELEGRAM_BOT_TOKEN='PASTE_YOUR_BOT_TOKEN' \
  -e TELEGRAM_BOT_USERNAME='your_bot_username' \
  -e TELEGRAM_PRIVATE_CHAT_ID='-1000000000000' \
  -e TELEGRAM_ADMIN_IDS='726773708,631884742' \
  -e TELEGRAM_SUPPORT_USERNAME='AreninaAnna' \
  -e TELEGRAM_START_PHOTO_PATH='/app/assets/1.jpg' \
  -e YOOKASSA_ENABLED='true' \
  -e YOOKASSA_SHOP_ID='your_shop_id' \
  -e YOOKASSA_SECRET_KEY='your_secret_key' \
  -e YOOKASSA_RETURN_URL='https://t.me/your_bot_username' \
  -e YOOKASSA_WEBHOOK_PATH='/api/webhooks/yookassa' \
  -e YOOKASSA_VAT_CODE='1' \
  -e YOOKASSA_PAYMENT_SUBJECT='service' \
  -e YOOKASSA_PAYMENT_MODE='full_payment' \
  -e APP_ZONE_ID='Europe/Moscow' \
  -e APP_DB_URL='jdbc:h2:file:/data/anna-bot;AUTO_SERVER=TRUE' \
  -e JAVA_TOOL_OPTIONS='-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false' \
  -v "$(pwd)/data:/data" \
  anna-bot
```

Если токен, который ты присылал, реальный, лучше сразу перевыпустить его в `@BotFather` и использовать уже новый токен.

Для твоего сервера с доменом `bot.umkarta.ru` пример будет таким:

```bash
docker build -t anna-bot .
docker rm -f anna-bot 2>/dev/null || true

docker run -d \
  --name anna-bot \
  --restart unless-stopped \
  --network host \
  --add-host api.telegram.org:149.154.167.220 \
  -e SERVER_PORT='8080' \
  -e TELEGRAM_BOT_TOKEN='PASTE_NEW_BOT_TOKEN' \
  -e TELEGRAM_BOT_USERNAME='your_bot_username' \
  -e TELEGRAM_PRIVATE_CHAT_ID='-1000000000000' \
  -e TELEGRAM_ADMIN_IDS='726773708,631884742' \
  -e TELEGRAM_SUPPORT_USERNAME='AreninaAnna' \
  -e TELEGRAM_START_PHOTO_PATH='/app/assets/1.jpg' \
  -e YOOKASSA_ENABLED='true' \
  -e YOOKASSA_SHOP_ID='your_shop_id' \
  -e YOOKASSA_SECRET_KEY='your_secret_key' \
  -e YOOKASSA_RETURN_URL='https://t.me/your_bot_username' \
  -e YOOKASSA_WEBHOOK_PATH='/api/webhooks/yookassa' \
  -e YOOKASSA_VAT_CODE='1' \
  -e YOOKASSA_PAYMENT_SUBJECT='service' \
  -e YOOKASSA_PAYMENT_MODE='full_payment' \
  -e APP_ZONE_ID='Europe/Moscow' \
  -e APP_DB_URL='jdbc:h2:file:/data/anna-bot;AUTO_SERVER=TRUE' \
  -e JAVA_TOOL_OPTIONS='-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false' \
  -v "$(pwd)/data:/data" \
  anna-bot
```

## Настройка ЮKassa

1. В личном кабинете ЮKassa укажи HTTPS URL webhook-а: `https://your-domain.tld/api/webhooks/yookassa`.
2. Подпиши магазин на событие `payment.succeeded`.
3. При желании добавь `payment.canceled`, чтобы получать уведомления об отменах.
4. Убедись, что в Telegram бот является админом закрытого чата и имеет право приглашать и удалять пользователей.

Для твоего домена webhook ЮKassa должен быть:

```text
https://bot.umkarta.ru/api/webhooks/yookassa
```

## Nginx

Если на `bot.umkarta.ru` уже висит мини-апп, не нужно отдавать весь домен новому контейнеру. Безопаснее добавить в текущий `server` блок только отдельный `location` под webhook:

```nginx
location /api/webhooks/yookassa {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

Так мини-апп останется на своих текущих `location`, а в контейнер бота будет ходить только ЮKassa webhook.

## Фото

Файл [assets/1.jpg](/Users/dmitry/Desktop/anna/assets/1.jpg) оставлен в проекте, если позже захочешь снова использовать его в сценариях бота.
