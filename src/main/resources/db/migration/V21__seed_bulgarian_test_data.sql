-- All seeded users use the same password: Parola2026!
--
-- Raw teacher invite tokens included in this seed:
--   svetla-angelova-2026
--   ivan-rusev-2026
--   desislava-mitev-2026
--   petya-stefanova-2026
--   dimitar-vasilev-2026

SET @seed_password_hash = '$2a$10$twZkofMQSnpxmmQs5lCFn.S0jSf9NkgQ0EYd1TLfjc/RZ9WFx1R9O';

-- users
INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'maria.nikolova@ppmg-vratsa.bg', @seed_password_hash, 'Мария', 'Николова', 'ADMIN', NULL, NULL,
       '2026-01-10 08:00:00', '2026-01-10 08:00:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'maria.nikolova@ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'daniela.georgieva@ppmg-vratsa.bg', @seed_password_hash, 'Даниела', 'Георгиева', 'TEACHER', NULL, NULL,
       '2026-01-11 08:10:00', '2026-01-11 08:10:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'daniela.georgieva@ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'nikolay.hristov@ppmg-vratsa.bg', @seed_password_hash, 'Николай', 'Христов', 'TEACHER', NULL, NULL,
       '2026-01-11 08:20:00', '2026-01-11 08:20:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'nikolay.hristov@ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'milena.todorova@ppmg-vratsa.bg', @seed_password_hash, 'Милена', 'Тодорова', 'TEACHER', NULL, NULL,
       '2026-01-11 08:30:00', '2026-01-11 08:30:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'milena.todorova@ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'elitsa.stoyanova@ppmg-vratsa.bg', @seed_password_hash, 'Елица', 'Стоянова', 'TEACHER', NULL, NULL,
       '2026-01-11 08:40:00', '2026-01-11 08:40:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'elitsa.stoyanova@ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'viktor.petrov@ppmg-vratsa.bg', @seed_password_hash, 'Виктор', 'Петров', 'TEACHER', NULL, NULL,
       '2026-01-11 08:50:00', '2026-01-11 08:50:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'viktor.petrov@ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'petar.ivanov@student.ppmg-vratsa.bg', @seed_password_hash, 'Петър', 'Иванов', 'STUDENT', 10, 'Б',
       '2026-01-15 09:00:00', '2026-01-15 09:00:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'petar.ivanov@student.ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'aleks.borisov@student.ppmg-vratsa.bg', @seed_password_hash, 'Алекс', 'Борисов', 'STUDENT', 11, 'А',
       '2026-01-15 09:10:00', '2026-01-15 09:10:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'aleks.borisov@student.ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'viktoriya.dimitrova@student.ppmg-vratsa.bg', @seed_password_hash, 'Виктория', 'Димитрова', 'STUDENT', 9, 'В',
       '2026-01-15 09:20:00', '2026-01-15 09:20:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'viktoriya.dimitrova@student.ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'simona.ilieva@student.ppmg-vratsa.bg', @seed_password_hash, 'Симона', 'Илиева', 'STUDENT', 12, 'А',
       '2026-01-15 09:30:00', '2026-01-15 09:30:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'simona.ilieva@student.ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'georgi.kolev@student.ppmg-vratsa.bg', @seed_password_hash, 'Георги', 'Колев', 'STUDENT', 8, 'Б',
       '2026-01-15 09:40:00', '2026-01-15 09:40:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'georgi.kolev@student.ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'niya.marinova@student.ppmg-vratsa.bg', @seed_password_hash, 'Ния', 'Маринова', 'STUDENT', 11, 'Б',
       '2026-01-15 09:50:00', '2026-01-15 09:50:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'niya.marinova@student.ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'stefan.genov@student.ppmg-vratsa.bg', @seed_password_hash, 'Стефан', 'Генов', 'STUDENT', 10, 'А',
       '2026-01-15 10:00:00', '2026-01-15 10:00:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'stefan.genov@student.ppmg-vratsa.bg');

INSERT INTO users (email, password_hash, first_name, last_name, role, grade, class_name, created_at, updated_at, deleted_at)
SELECT 'radostina.peneva@student.ppmg-vratsa.bg', @seed_password_hash, 'Радостина', 'Пенева', 'STUDENT', 9, 'А',
       '2026-01-15 10:10:00', '2026-01-15 10:10:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'radostina.peneva@student.ppmg-vratsa.bg');

-- clubs
INSERT INTO clubs (name, description, schedule_text, room, contact_email, contact_phone, is_active, created_by, created_at, updated_at, deleted_at, main_image_url)
SELECT 'Клуб по роботика и автоматизация',
       'Практически клуб за ученици с интерес към роботика, електроника и програмиране на микроконтролери. Работи по проекти за училищни изложения и състезания.',
       'Всеки вторник и четвъртък от 15:30 ч.',
       'Кабинет 205',
       'robotika@ppmg-vratsa.bg',
       '+359 92 660 512',
       1,
       creator.id,
       '2026-02-01 08:30:00',
       '2026-02-01 08:30:00',
       NULL,
       'https://assets.school-clubs.bg/clubs/robotika/main.jpg'
FROM users creator
WHERE creator.email = 'maria.nikolova@ppmg-vratsa.bg'
  AND NOT EXISTS (SELECT 1 FROM clubs WHERE name = 'Клуб по роботика и автоматизация');

INSERT INTO clubs (name, description, schedule_text, room, contact_email, contact_phone, is_active, created_by, created_at, updated_at, deleted_at, main_image_url)
SELECT 'Театрална студия "Сцена"',
       'Клуб за сценична реч, актьорско майсторство и работа по училищни спектакли. Учениците подготвят открити репетиции и участие в празнични програми.',
       'Всяка сряда от 16:00 ч.',
       'Актова зала',
       'teatar@ppmg-vratsa.bg',
       '+359 92 660 518',
       1,
       creator.id,
       '2026-02-02 10:00:00',
       '2026-02-02 10:00:00',
       NULL,
       'https://assets.school-clubs.bg/clubs/teatar/main.jpg'
FROM users creator
WHERE creator.email = 'nikolay.hristov@ppmg-vratsa.bg'
  AND NOT EXISTS (SELECT 1 FROM clubs WHERE name = 'Театрална студия "Сцена"');

INSERT INTO clubs (name, description, schedule_text, room, contact_email, contact_phone, is_active, created_by, created_at, updated_at, deleted_at, main_image_url)
SELECT 'Астрономически клуб "Хелиос"',
       'Клуб за наблюдение на нощното небе, работа с телескоп и подготовка за олимпиади и ученически проекти по физика и астрономия.',
       'Петък от 18:30 ч. при ясно време',
       'Физичен кабинет 301',
       'helios@ppmg-vratsa.bg',
       '+359 92 660 523',
       1,
       creator.id,
       '2026-02-03 11:15:00',
       '2026-02-03 11:15:00',
       NULL,
       'https://assets.school-clubs.bg/clubs/helios/main.jpg'
FROM users creator
WHERE creator.email = 'milena.todorova@ppmg-vratsa.bg'
  AND NOT EXISTS (SELECT 1 FROM clubs WHERE name = 'Астрономически клуб "Хелиос"');

INSERT INTO clubs (name, description, schedule_text, room, contact_email, contact_phone, is_active, created_by, created_at, updated_at, deleted_at, main_image_url)
SELECT 'Доброволчески клуб "Будители"',
       'Доброволчески екип за кампании в подкрепа на училищната общност, събиране на книги и участие в местни благотворителни инициативи.',
       'Понеделник от 14:30 ч.',
       'Библиотека',
       'dobrovolci@ppmg-vratsa.bg',
       '+359 92 660 527',
       1,
       creator.id,
       '2026-02-04 09:45:00',
       '2026-02-04 09:45:00',
       NULL,
       'https://assets.school-clubs.bg/clubs/buditeli/main.jpg'
FROM users creator
WHERE creator.email = 'elitsa.stoyanova@ppmg-vratsa.bg'
  AND NOT EXISTS (SELECT 1 FROM clubs WHERE name = 'Доброволчески клуб "Будители"');

INSERT INTO clubs (name, description, schedule_text, room, contact_email, contact_phone, is_active, created_by, created_at, updated_at, deleted_at, main_image_url)
SELECT 'Дебатьорски клуб "Аргумент"',
       'Клуб за развитие на публично говорене, аналитично мислене и участие в вътрешноучилищни и междуучилищни дебати.',
       'Четвъртък от 16:10 ч.',
       'Кабинет 114',
       'debati@ppmg-vratsa.bg',
       '+359 92 660 531',
       1,
       creator.id,
       '2026-02-05 12:20:00',
       '2026-02-05 12:20:00',
       NULL,
       'https://assets.school-clubs.bg/clubs/argument/main.jpg'
FROM users creator
WHERE creator.email = 'viktor.petrov@ppmg-vratsa.bg'
  AND NOT EXISTS (SELECT 1 FROM clubs WHERE name = 'Дебатьорски клуб "Аргумент"');

-- club_teachers
INSERT INTO club_teachers (club_id, teacher_user_id, created_at, updated_at, deleted_at)
SELECT c.id, t.id, '2026-02-06 08:00:00', '2026-02-06 08:00:00', NULL
FROM clubs c
JOIN users t ON t.email = 'daniela.georgieva@ppmg-vratsa.bg'
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (
      SELECT 1 FROM club_teachers ct WHERE ct.club_id = c.id AND ct.teacher_user_id = t.id
  );

INSERT INTO club_teachers (club_id, teacher_user_id, created_at, updated_at, deleted_at)
SELECT c.id, t.id, '2026-02-06 08:05:00', '2026-02-06 08:05:00', NULL
FROM clubs c
JOIN users t ON t.email = 'milena.todorova@ppmg-vratsa.bg'
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (
      SELECT 1 FROM club_teachers ct WHERE ct.club_id = c.id AND ct.teacher_user_id = t.id
  );

INSERT INTO club_teachers (club_id, teacher_user_id, created_at, updated_at, deleted_at)
SELECT c.id, t.id, '2026-02-06 08:10:00', '2026-02-06 08:10:00', NULL
FROM clubs c
JOIN users t ON t.email = 'nikolay.hristov@ppmg-vratsa.bg'
WHERE c.name = 'Театрална студия "Сцена"'
  AND NOT EXISTS (
      SELECT 1 FROM club_teachers ct WHERE ct.club_id = c.id AND ct.teacher_user_id = t.id
  );

INSERT INTO club_teachers (club_id, teacher_user_id, created_at, updated_at, deleted_at)
SELECT c.id, t.id, '2026-02-06 08:15:00', '2026-02-06 08:15:00', NULL
FROM clubs c
JOIN users t ON t.email = 'milena.todorova@ppmg-vratsa.bg'
WHERE c.name = 'Астрономически клуб "Хелиос"'
  AND NOT EXISTS (
      SELECT 1 FROM club_teachers ct WHERE ct.club_id = c.id AND ct.teacher_user_id = t.id
  );

INSERT INTO club_teachers (club_id, teacher_user_id, created_at, updated_at, deleted_at)
SELECT c.id, t.id, '2026-02-06 08:20:00', '2026-02-06 08:20:00', NULL
FROM clubs c
JOIN users t ON t.email = 'elitsa.stoyanova@ppmg-vratsa.bg'
WHERE c.name = 'Доброволчески клуб "Будители"'
  AND NOT EXISTS (
      SELECT 1 FROM club_teachers ct WHERE ct.club_id = c.id AND ct.teacher_user_id = t.id
  );

INSERT INTO club_teachers (club_id, teacher_user_id, created_at, updated_at, deleted_at)
SELECT c.id, t.id, '2026-02-06 08:25:00', '2026-02-06 08:25:00', NULL
FROM clubs c
JOIN users t ON t.email = 'viktor.petrov@ppmg-vratsa.bg'
WHERE c.name = 'Дебатьорски клуб "Аргумент"'
  AND NOT EXISTS (
      SELECT 1 FROM club_teachers ct WHERE ct.club_id = c.id AND ct.teacher_user_id = t.id
  );

-- club_media
INSERT INTO club_media (club_id, url, sort_order, created_at, updated_at, deleted_at)
SELECT c.id, 'https://assets.school-clubs.bg/clubs/robotika/workbench.jpg', 0, '2026-02-07 10:00:00', '2026-02-07 10:00:00', NULL
FROM clubs c
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (SELECT 1 FROM club_media cm WHERE cm.club_id = c.id AND cm.url = 'https://assets.school-clubs.bg/clubs/robotika/workbench.jpg');

INSERT INTO club_media (club_id, url, sort_order, created_at, updated_at, deleted_at)
SELECT c.id, 'https://assets.school-clubs.bg/clubs/robotika/team-project.jpg', 1, '2026-02-07 10:02:00', '2026-02-07 10:02:00', NULL
FROM clubs c
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (SELECT 1 FROM club_media cm WHERE cm.club_id = c.id AND cm.url = 'https://assets.school-clubs.bg/clubs/robotika/team-project.jpg');

INSERT INTO club_media (club_id, url, sort_order, created_at, updated_at, deleted_at)
SELECT c.id, 'https://assets.school-clubs.bg/clubs/teatar/rehearsal.jpg', 0, '2026-02-07 10:05:00', '2026-02-07 10:05:00', NULL
FROM clubs c
WHERE c.name = 'Театрална студия "Сцена"'
  AND NOT EXISTS (SELECT 1 FROM club_media cm WHERE cm.club_id = c.id AND cm.url = 'https://assets.school-clubs.bg/clubs/teatar/rehearsal.jpg');

INSERT INTO club_media (club_id, url, sort_order, created_at, updated_at, deleted_at)
SELECT c.id, 'https://assets.school-clubs.bg/clubs/teatar/costumes.jpg', 1, '2026-02-07 10:07:00', '2026-02-07 10:07:00', NULL
FROM clubs c
WHERE c.name = 'Театрална студия "Сцена"'
  AND NOT EXISTS (SELECT 1 FROM club_media cm WHERE cm.club_id = c.id AND cm.url = 'https://assets.school-clubs.bg/clubs/teatar/costumes.jpg');

INSERT INTO club_media (club_id, url, sort_order, created_at, updated_at, deleted_at)
SELECT c.id, 'https://assets.school-clubs.bg/clubs/helios/telescope-night.jpg', 0, '2026-02-07 10:10:00', '2026-02-07 10:10:00', NULL
FROM clubs c
WHERE c.name = 'Астрономически клуб "Хелиос"'
  AND NOT EXISTS (SELECT 1 FROM club_media cm WHERE cm.club_id = c.id AND cm.url = 'https://assets.school-clubs.bg/clubs/helios/telescope-night.jpg');

INSERT INTO club_media (club_id, url, sort_order, created_at, updated_at, deleted_at)
SELECT c.id, 'https://assets.school-clubs.bg/clubs/buditeli/book-drive.jpg', 0, '2026-02-07 10:12:00', '2026-02-07 10:12:00', NULL
FROM clubs c
WHERE c.name = 'Доброволчески клуб "Будители"'
  AND NOT EXISTS (SELECT 1 FROM club_media cm WHERE cm.club_id = c.id AND cm.url = 'https://assets.school-clubs.bg/clubs/buditeli/book-drive.jpg');

INSERT INTO club_media (club_id, url, sort_order, created_at, updated_at, deleted_at)
SELECT c.id, 'https://assets.school-clubs.bg/clubs/argument/training-round.jpg', 0, '2026-02-07 10:15:00', '2026-02-07 10:15:00', NULL
FROM clubs c
WHERE c.name = 'Дебатьорски клуб "Аргумент"'
  AND NOT EXISTS (SELECT 1 FROM club_media cm WHERE cm.club_id = c.id AND cm.url = 'https://assets.school-clubs.bg/clubs/argument/training-round.jpg');

INSERT INTO club_media (club_id, url, sort_order, created_at, updated_at, deleted_at)
SELECT c.id, 'https://assets.school-clubs.bg/clubs/argument/public-speaking.jpg', 1, '2026-02-07 10:17:00', '2026-02-07 10:17:00', NULL
FROM clubs c
WHERE c.name = 'Дебатьорски клуб "Аргумент"'
  AND NOT EXISTS (SELECT 1 FROM club_media cm WHERE cm.club_id = c.id AND cm.url = 'https://assets.school-clubs.bg/clubs/argument/public-speaking.jpg');

-- club_memberships
INSERT INTO club_memberships (club_id, student_user_id, status, joined_at, left_at, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'ACTIVE', '2026-02-10 15:40:00', NULL, '2026-02-10 15:40:00', '2026-02-10 15:40:00', NULL
FROM clubs c
JOIN users s ON s.email = 'petar.ivanov@student.ppmg-vratsa.bg'
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (SELECT 1 FROM club_memberships cm WHERE cm.club_id = c.id AND cm.student_user_id = s.id);

INSERT INTO club_memberships (club_id, student_user_id, status, joined_at, left_at, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'ACTIVE', '2026-02-18 16:00:00', NULL, '2026-02-18 16:00:00', '2026-02-18 16:00:00', NULL
FROM clubs c
JOIN users s ON s.email = 'aleks.borisov@student.ppmg-vratsa.bg'
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (SELECT 1 FROM club_memberships cm WHERE cm.club_id = c.id AND cm.student_user_id = s.id);

INSERT INTO club_memberships (club_id, student_user_id, status, joined_at, left_at, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'ACTIVE', '2026-02-20 16:30:00', NULL, '2026-02-20 16:30:00', '2026-02-20 16:30:00', NULL
FROM clubs c
JOIN users s ON s.email = 'viktoriya.dimitrova@student.ppmg-vratsa.bg'
WHERE c.name = 'Театрална студия "Сцена"'
  AND NOT EXISTS (SELECT 1 FROM club_memberships cm WHERE cm.club_id = c.id AND cm.student_user_id = s.id);

INSERT INTO club_memberships (club_id, student_user_id, status, joined_at, left_at, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'ACTIVE', '2026-03-01 18:45:00', NULL, '2026-03-01 18:45:00', '2026-03-01 18:45:00', NULL
FROM clubs c
JOIN users s ON s.email = 'niya.marinova@student.ppmg-vratsa.bg'
WHERE c.name = 'Астрономически клуб "Хелиос"'
  AND NOT EXISTS (SELECT 1 FROM club_memberships cm WHERE cm.club_id = c.id AND cm.student_user_id = s.id);

INSERT INTO club_memberships (club_id, student_user_id, status, joined_at, left_at, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'ACTIVE', '2026-03-12 14:40:00', NULL, '2026-03-12 14:40:00', '2026-03-12 14:40:00', NULL
FROM clubs c
JOIN users s ON s.email = 'georgi.kolev@student.ppmg-vratsa.bg'
WHERE c.name = 'Доброволчески клуб "Будители"'
  AND NOT EXISTS (SELECT 1 FROM club_memberships cm WHERE cm.club_id = c.id AND cm.student_user_id = s.id);

INSERT INTO club_memberships (club_id, student_user_id, status, joined_at, left_at, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'ACTIVE', '2026-03-15 16:20:00', NULL, '2026-03-15 16:20:00', '2026-03-15 16:20:00', NULL
FROM clubs c
JOIN users s ON s.email = 'simona.ilieva@student.ppmg-vratsa.bg'
WHERE c.name = 'Дебатьорски клуб "Аргумент"'
  AND NOT EXISTS (SELECT 1 FROM club_memberships cm WHERE cm.club_id = c.id AND cm.student_user_id = s.id);

INSERT INTO club_memberships (club_id, student_user_id, status, joined_at, left_at, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'LEFT', '2026-01-25 18:30:00', '2026-03-30 19:10:00', '2026-01-25 18:30:00', '2026-03-30 19:10:00', NULL
FROM clubs c
JOIN users s ON s.email = 'stefan.genov@student.ppmg-vratsa.bg'
WHERE c.name = 'Астрономически клуб "Хелиос"'
  AND NOT EXISTS (SELECT 1 FROM club_memberships cm WHERE cm.club_id = c.id AND cm.student_user_id = s.id);

INSERT INTO club_memberships (club_id, student_user_id, status, joined_at, left_at, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'BANNED', '2026-02-05 16:15:00', '2026-04-12 17:00:00', '2026-02-05 16:15:00', '2026-04-12 17:00:00', NULL
FROM clubs c
JOIN users s ON s.email = 'radostina.peneva@student.ppmg-vratsa.bg'
WHERE c.name = 'Театрална студия "Сцена"'
  AND NOT EXISTS (SELECT 1 FROM club_memberships cm WHERE cm.club_id = c.id AND cm.student_user_id = s.id);

-- club_membership_requests
INSERT INTO club_membership_requests (club_id, student_user_id, status, message, decided_by, decided_at, decision_note, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'APPROVED',
       'Искам да се включа в практическите проекти по електроника и да участвам в РобоФест.',
       d.id,
       '2026-02-10 15:30:00',
       'Показва постоянство в часовете по информатика и добра работа в екип.',
       '2026-02-08 13:15:00',
       '2026-02-10 15:30:00',
       NULL
FROM clubs c
JOIN users s ON s.email = 'petar.ivanov@student.ppmg-vratsa.bg'
JOIN users d ON d.email = 'daniela.georgieva@ppmg-vratsa.bg'
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (
      SELECT 1
      FROM club_membership_requests r
      WHERE r.club_id = c.id
        AND r.student_user_id = s.id
        AND r.status = 'APPROVED'
        AND r.message = 'Искам да се включа в практическите проекти по електроника и да участвам в РобоФест.'
  );

INSERT INTO club_membership_requests (club_id, student_user_id, status, message, decided_by, decided_at, decision_note, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'APPROVED',
       'Театърът ми помага да говоря по-уверено и искам да участвам в училищния спектакъл.',
       d.id,
       '2026-02-20 16:20:00',
       'Приета след успешно участие в открита репетиция.',
       '2026-02-17 12:00:00',
       '2026-02-20 16:20:00',
       NULL
FROM clubs c
JOIN users s ON s.email = 'viktoriya.dimitrova@student.ppmg-vratsa.bg'
JOIN users d ON d.email = 'nikolay.hristov@ppmg-vratsa.bg'
WHERE c.name = 'Театрална студия "Сцена"'
  AND NOT EXISTS (
      SELECT 1
      FROM club_membership_requests r
      WHERE r.club_id = c.id
        AND r.student_user_id = s.id
        AND r.status = 'APPROVED'
        AND r.message = 'Театърът ми помага да говоря по-уверено и искам да участвам в училищния спектакъл.'
  );

INSERT INTO club_membership_requests (club_id, student_user_id, status, message, decided_by, decided_at, decision_note, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'APPROVED',
       'Искам да участвам в доброволчески акции и кампании за събиране на книги.',
       d.id,
       '2026-03-12 14:35:00',
       'Одобрен след разговор за ангажираност и участие в училищни инициативи.',
       '2026-03-09 11:20:00',
       '2026-03-12 14:35:00',
       NULL
FROM clubs c
JOIN users s ON s.email = 'georgi.kolev@student.ppmg-vratsa.bg'
JOIN users d ON d.email = 'elitsa.stoyanova@ppmg-vratsa.bg'
WHERE c.name = 'Доброволчески клуб "Будители"'
  AND NOT EXISTS (
      SELECT 1
      FROM club_membership_requests r
      WHERE r.club_id = c.id
        AND r.student_user_id = s.id
        AND r.status = 'APPROVED'
        AND r.message = 'Искам да участвам в доброволчески акции и кампании за събиране на книги.'
  );

INSERT INTO club_membership_requests (club_id, student_user_id, status, message, decided_by, decided_at, decision_note, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'REJECTED',
       'Интересувам се от наблюдения с телескоп, но тази пролет имам много подготовки за олимпиади.',
       d.id,
       '2026-03-18 17:45:00',
       'Предложено е кандидатстване отново през есента след освобождаване на места.',
       '2026-03-16 10:10:00',
       '2026-03-18 17:45:00',
       NULL
FROM clubs c
JOIN users s ON s.email = 'aleks.borisov@student.ppmg-vratsa.bg'
JOIN users d ON d.email = 'milena.todorova@ppmg-vratsa.bg'
WHERE c.name = 'Астрономически клуб "Хелиос"'
  AND NOT EXISTS (
      SELECT 1
      FROM club_membership_requests r
      WHERE r.club_id = c.id
        AND r.student_user_id = s.id
        AND r.status = 'REJECTED'
        AND r.message = 'Интересувам се от наблюдения с телескоп, но тази пролет имам много подготовки за олимпиади.'
  );

INSERT INTO club_membership_requests (club_id, student_user_id, status, message, decided_by, decided_at, decision_note, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'CANCELLED',
       'Искам да помогна при подреждането на дарените книги и при пролетната кампания.',
       NULL,
       NULL,
       NULL,
       '2026-03-21 09:30:00',
       '2026-03-24 08:15:00',
       NULL
FROM clubs c
JOIN users s ON s.email = 'niya.marinova@student.ppmg-vratsa.bg'
WHERE c.name = 'Доброволчески клуб "Будители"'
  AND NOT EXISTS (
      SELECT 1
      FROM club_membership_requests r
      WHERE r.club_id = c.id
        AND r.student_user_id = s.id
        AND r.status = 'CANCELLED'
        AND r.message = 'Искам да помогна при подреждането на дарените книги и при пролетната кампания.'
  );

INSERT INTO club_membership_requests (club_id, student_user_id, status, message, decided_by, decided_at, decision_note, created_at, updated_at, deleted_at)
SELECT c.id, s.id, 'PENDING',
       'Искам да развия уменията си за аргументация и публично говорене пред публика.',
       NULL,
       NULL,
       NULL,
       '2026-04-22 13:05:00',
       '2026-04-22 13:05:00',
       NULL
FROM clubs c
JOIN users s ON s.email = 'stefan.genov@student.ppmg-vratsa.bg'
WHERE c.name = 'Дебатьорски клуб "Аргумент"'
  AND NOT EXISTS (
      SELECT 1
      FROM club_membership_requests r
      WHERE r.club_id = c.id
        AND r.student_user_id = s.id
        AND r.status = 'PENDING'
        AND r.message = 'Искам да развия уменията си за аргументация и публично говорене пред публика.'
  );

-- events
INSERT INTO events (club_id, title, description, start_at, end_at, location, main_image_url, capacity, registration_deadline, status, audience, created_by, created_at, updated_at, deleted_at)
SELECT c.id,
       'Работилница по Arduino за начинаещи',
       'Практическо занимание за свързване на сензори, управление на светодиоди и първи стъпки в работата с Arduino.',
       '2026-05-12 15:30:00',
       '2026-05-12 17:30:00',
       'Кабинет 205',
       'https://assets.school-clubs.bg/events/arduino-workshop.jpg',
       18,
       '2026-05-11 18:00:00',
       'PUBLISHED',
       'ALL_STUDENTS',
       u.id,
       '2026-04-24 09:00:00',
       '2026-04-24 09:00:00',
       NULL
FROM clubs c
JOIN users u ON u.email = 'daniela.georgieva@ppmg-vratsa.bg'
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.club_id = c.id AND e.title = 'Работилница по Arduino за начинаещи');

INSERT INTO events (club_id, title, description, start_at, end_at, location, main_image_url, capacity, registration_deadline, status, audience, created_by, created_at, updated_at, deleted_at)
SELECT c.id,
       'РобоФест Враца 2026',
       'Вътрешноучилищно представяне на проектите по роботика с демонстрации на автономни модели и кратки екипни презентации.',
       '2026-05-24 10:00:00',
       '2026-05-24 16:00:00',
       'Фоайе на училището',
       'https://assets.school-clubs.bg/events/robofest-vratsa-2026.jpg',
       60,
       '2026-05-22 18:00:00',
       'PUBLISHED',
       'ALL_STUDENTS',
       u.id,
       '2026-04-25 10:30:00',
       '2026-04-25 10:30:00',
       NULL
FROM clubs c
JOIN users u ON u.email = 'daniela.georgieva@ppmg-vratsa.bg'
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.club_id = c.id AND e.title = 'РобоФест Враца 2026');

INSERT INTO events (club_id, title, description, start_at, end_at, location, main_image_url, capacity, registration_deadline, status, audience, created_by, created_at, updated_at, deleted_at)
SELECT c.id,
       'Нощ на телескопите',
       'Вечерно наблюдение на Луната и пролетните съзвездия с кратка беседа за ориентиране по небето.',
       '2026-05-21 20:30:00',
       '2026-05-21 22:30:00',
       'Покривна тераса на училището',
       'https://assets.school-clubs.bg/events/night-of-telescopes.jpg',
       15,
       '2026-05-20 18:00:00',
       'PUBLISHED',
       'MEMBERS_ONLY',
       u.id,
       '2026-04-23 18:15:00',
       '2026-04-23 18:15:00',
       NULL
FROM clubs c
JOIN users u ON u.email = 'milena.todorova@ppmg-vratsa.bg'
WHERE c.name = 'Астрономически клуб "Хелиос"'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.club_id = c.id AND e.title = 'Нощ на телескопите');

INSERT INTO events (club_id, title, description, start_at, end_at, location, main_image_url, capacity, registration_deadline, status, audience, created_by, created_at, updated_at, deleted_at)
SELECT c.id,
       'Ден на доброто в квартал Дъбника',
       'Доброволческа инициатива за почистване на зелени площи и раздаване на информационни материали за разделно събиране.',
       '2026-04-18 09:00:00',
       '2026-04-18 13:00:00',
       'Квартал Дъбника',
       'https://assets.school-clubs.bg/events/day-of-kindness.jpg',
       NULL,
       '2026-04-16 18:00:00',
       'PUBLISHED',
       'ALL_STUDENTS',
       u.id,
       '2026-04-01 12:00:00',
       '2026-04-01 12:00:00',
       NULL
FROM clubs c
JOIN users u ON u.email = 'elitsa.stoyanova@ppmg-vratsa.bg'
WHERE c.name = 'Доброволчески клуб "Будители"'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.club_id = c.id AND e.title = 'Ден на доброто в квартал Дъбника');

INSERT INTO events (club_id, title, description, start_at, end_at, location, main_image_url, capacity, registration_deadline, status, audience, created_by, created_at, updated_at, deleted_at)
SELECT c.id,
       'Училищен дебат: Технологиите в класната стая',
       'Открит дебат между ученически отбори по темата за ролята на технологиите в учебния процес.',
       '2026-05-29 14:30:00',
       '2026-05-29 16:30:00',
       'Кабинет 114',
       'https://assets.school-clubs.bg/events/school-debate-tech.jpg',
       24,
       '2026-05-28 12:00:00',
       'PUBLISHED',
       'ALL_STUDENTS',
       u.id,
       '2026-04-26 09:10:00',
       '2026-04-26 09:10:00',
       NULL
FROM clubs c
JOIN users u ON u.email = 'viktor.petrov@ppmg-vratsa.bg'
WHERE c.name = 'Дебатьорски клуб "Аргумент"'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.club_id = c.id AND e.title = 'Училищен дебат: Технологиите в класната стая');

INSERT INTO events (club_id, title, description, start_at, end_at, location, main_image_url, capacity, registration_deadline, status, audience, created_by, created_at, updated_at, deleted_at)
SELECT c.id,
       'Генерална репетиция на "Под игото"',
       'Работна репетиция със сценични преходи, осветление и разпределение на последните задачи преди представянето.',
       '2026-05-14 16:00:00',
       '2026-05-14 18:30:00',
       'Актова зала',
       'https://assets.school-clubs.bg/events/general-rehearsal.jpg',
       12,
       '2026-05-13 17:00:00',
       'DRAFT',
       'MEMBERS_ONLY',
       u.id,
       '2026-04-27 08:45:00',
       '2026-04-27 08:45:00',
       NULL
FROM clubs c
JOIN users u ON u.email = 'nikolay.hristov@ppmg-vratsa.bg'
WHERE c.name = 'Театрална студия "Сцена"'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.club_id = c.id AND e.title = 'Генерална репетиция на "Под игото"');

INSERT INTO events (club_id, title, description, start_at, end_at, location, main_image_url, capacity, registration_deadline, status, audience, created_by, created_at, updated_at, deleted_at)
SELECT c.id,
       'Среща с алумни инженери',
       'Планирана среща с бивши възпитаници на училището, работещи в инженерни специалности.',
       '2026-06-05 15:00:00',
       '2026-06-05 17:00:00',
       'Конферентна зала',
       'https://assets.school-clubs.bg/events/alumni-engineers.jpg',
       30,
       '2026-06-03 18:00:00',
       'CANCELLED',
       'ALL_STUDENTS',
       u.id,
       '2026-04-28 11:20:00',
       '2026-04-29 09:00:00',
       NULL
FROM clubs c
JOIN users u ON u.email = 'maria.nikolova@ppmg-vratsa.bg'
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.club_id = c.id AND e.title = 'Среща с алумни инженери');

-- event_registrations
INSERT INTO event_registrations (event_id, student_user_id, status, registered_at, cancelled_at, created_at, updated_at, deleted_at)
SELECT e.id, s.id, 'REGISTERED', '2026-04-29 08:45:00', NULL, '2026-04-29 08:45:00', '2026-04-29 08:45:00', NULL
FROM events e
JOIN users s ON s.email = 'petar.ivanov@student.ppmg-vratsa.bg'
WHERE e.title = 'Работилница по Arduino за начинаещи'
  AND NOT EXISTS (SELECT 1 FROM event_registrations er WHERE er.event_id = e.id AND er.student_user_id = s.id);

INSERT INTO event_registrations (event_id, student_user_id, status, registered_at, cancelled_at, created_at, updated_at, deleted_at)
SELECT e.id, s.id, 'REGISTERED', '2026-04-29 09:00:00', NULL, '2026-04-29 09:00:00', '2026-04-29 09:00:00', NULL
FROM events e
JOIN users s ON s.email = 'aleks.borisov@student.ppmg-vratsa.bg'
WHERE e.title = 'Работилница по Arduino за начинаещи'
  AND NOT EXISTS (SELECT 1 FROM event_registrations er WHERE er.event_id = e.id AND er.student_user_id = s.id);

INSERT INTO event_registrations (event_id, student_user_id, status, registered_at, cancelled_at, created_at, updated_at, deleted_at)
SELECT e.id, s.id, 'REGISTERED', '2026-04-29 09:10:00', NULL, '2026-04-29 09:10:00', '2026-04-29 09:10:00', NULL
FROM events e
JOIN users s ON s.email = 'petar.ivanov@student.ppmg-vratsa.bg'
WHERE e.title = 'РобоФест Враца 2026'
  AND NOT EXISTS (SELECT 1 FROM event_registrations er WHERE er.event_id = e.id AND er.student_user_id = s.id);

INSERT INTO event_registrations (event_id, student_user_id, status, registered_at, cancelled_at, created_at, updated_at, deleted_at)
SELECT e.id, s.id, 'REGISTERED', '2026-04-28 21:15:00', NULL, '2026-04-28 21:15:00', '2026-04-28 21:15:00', NULL
FROM events e
JOIN users s ON s.email = 'niya.marinova@student.ppmg-vratsa.bg'
WHERE e.title = 'Нощ на телескопите'
  AND NOT EXISTS (SELECT 1 FROM event_registrations er WHERE er.event_id = e.id AND er.student_user_id = s.id);

INSERT INTO event_registrations (event_id, student_user_id, status, registered_at, cancelled_at, created_at, updated_at, deleted_at)
SELECT e.id, s.id, 'REGISTERED', '2026-04-11 12:00:00', NULL, '2026-04-11 12:00:00', '2026-04-11 12:00:00', NULL
FROM events e
JOIN users s ON s.email = 'georgi.kolev@student.ppmg-vratsa.bg'
WHERE e.title = 'Ден на доброто в квартал Дъбника'
  AND NOT EXISTS (SELECT 1 FROM event_registrations er WHERE er.event_id = e.id AND er.student_user_id = s.id);

INSERT INTO event_registrations (event_id, student_user_id, status, registered_at, cancelled_at, created_at, updated_at, deleted_at)
SELECT e.id, s.id, 'CANCELLED', '2026-04-10 16:20:00', '2026-04-15 17:05:00', '2026-04-10 16:20:00', '2026-04-15 17:05:00', NULL
FROM events e
JOIN users s ON s.email = 'simona.ilieva@student.ppmg-vratsa.bg'
WHERE e.title = 'Ден на доброто в квартал Дъбника'
  AND NOT EXISTS (SELECT 1 FROM event_registrations er WHERE er.event_id = e.id AND er.student_user_id = s.id);

INSERT INTO event_registrations (event_id, student_user_id, status, registered_at, cancelled_at, created_at, updated_at, deleted_at)
SELECT e.id, s.id, 'REGISTERED', '2026-04-28 14:10:00', NULL, '2026-04-28 14:10:00', '2026-04-28 14:10:00', NULL
FROM events e
JOIN users s ON s.email = 'viktoriya.dimitrova@student.ppmg-vratsa.bg'
WHERE e.title = 'Училищен дебат: Технологиите в класната стая'
  AND NOT EXISTS (SELECT 1 FROM event_registrations er WHERE er.event_id = e.id AND er.student_user_id = s.id);

-- announcements
INSERT INTO announcements (club_id, title, body, is_published, published_at, author_id, created_at, updated_at, deleted_at)
SELECT c.id,
       'Започва подготовката за РобоФест',
       'От следващата седмица започваме интензивни срещи по екипи. Всеки участник трябва да подготви кратък отчет за текущия етап на проекта си.',
       1,
       '2026-04-22 08:30:00',
       a.id,
       '2026-04-22 08:10:00',
       '2026-04-22 08:30:00',
       NULL
FROM clubs c
JOIN users a ON a.email = 'daniela.georgieva@ppmg-vratsa.bg'
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (SELECT 1 FROM announcements an WHERE an.club_id = c.id AND an.title = 'Започва подготовката за РобоФест');

INSERT INTO announcements (club_id, title, body, is_published, published_at, author_id, created_at, updated_at, deleted_at)
SELECT c.id,
       'Кастинг за нови роли през май',
       'На 6 май ще проведем кратък кастинг за нови роли и дубльори. Подгответе монолог до две минути и удобни дрехи за движение.',
       1,
       '2026-04-19 17:20:00',
       a.id,
       '2026-04-19 16:55:00',
       '2026-04-19 17:20:00',
       NULL
FROM clubs c
JOIN users a ON a.email = 'nikolay.hristov@ppmg-vratsa.bg'
WHERE c.name = 'Театрална студия "Сцена"'
  AND NOT EXISTS (SELECT 1 FROM announcements an WHERE an.club_id = c.id AND an.title = 'Кастинг за нови роли през май');

INSERT INTO announcements (club_id, title, body, is_published, published_at, author_id, created_at, updated_at, deleted_at)
SELECT c.id,
       'Наблюдение на метеорния поток Ета Аквариди',
       'При ясно време ще се съберем на покрива на училището след 20:15 ч. Носете топли дрехи и фенерче с червена светлина.',
       1,
       '2026-04-24 19:00:00',
       a.id,
       '2026-04-24 18:35:00',
       '2026-04-24 19:00:00',
       NULL
FROM clubs c
JOIN users a ON a.email = 'milena.todorova@ppmg-vratsa.bg'
WHERE c.name = 'Астрономически клуб "Хелиос"'
  AND NOT EXISTS (SELECT 1 FROM announcements an WHERE an.club_id = c.id AND an.title = 'Наблюдение на метеорния поток Ета Аквариди');

INSERT INTO announcements (club_id, title, body, is_published, published_at, author_id, created_at, updated_at, deleted_at)
SELECT c.id,
       'Събиране на книги за училищната библиотека',
       'Подготвяме майска кампания за събиране на запазени книги. До края на седмицата ще обявим график за приемането им.',
       0,
       NULL,
       a.id,
       '2026-04-27 11:40:00',
       '2026-04-27 11:40:00',
       NULL
FROM clubs c
JOIN users a ON a.email = 'elitsa.stoyanova@ppmg-vratsa.bg'
WHERE c.name = 'Доброволчески клуб "Будители"'
  AND NOT EXISTS (SELECT 1 FROM announcements an WHERE an.club_id = c.id AND an.title = 'Събиране на книги за училищната библиотека');

INSERT INTO announcements (club_id, title, body, is_published, published_at, author_id, created_at, updated_at, deleted_at)
SELECT c.id,
       'Вътрешен турнир по дебати през май',
       'На 14 май ще проведем вътрешен тренировъчен турнир с кратки подготвителни речи. Отборите ще бъдат обявени два дни по-рано.',
       1,
       '2026-04-26 12:15:00',
       a.id,
       '2026-04-26 11:55:00',
       '2026-04-26 12:15:00',
       NULL
FROM clubs c
JOIN users a ON a.email = 'viktor.petrov@ppmg-vratsa.bg'
WHERE c.name = 'Дебатьорски клуб "Аргумент"'
  AND NOT EXISTS (SELECT 1 FROM announcements an WHERE an.club_id = c.id AND an.title = 'Вътрешен турнир по дебати през май');

INSERT INTO announcements (club_id, title, body, is_published, published_at, author_id, created_at, updated_at, deleted_at)
SELECT c.id,
       'Доставка на нови сензори и материали',
       'Поръчката за ултразвукови сензори, сервомотори и макетни платки е потвърдена. Очакваме доставката преди следващата работилница.',
       1,
       '2026-04-28 09:45:00',
       a.id,
       '2026-04-28 09:20:00',
       '2026-04-28 09:45:00',
       NULL
FROM clubs c
JOIN users a ON a.email = 'maria.nikolova@ppmg-vratsa.bg'
WHERE c.name = 'Клуб по роботика и автоматизация'
  AND NOT EXISTS (SELECT 1 FROM announcements an WHERE an.club_id = c.id AND an.title = 'Доставка на нови сензори и материали');

-- teacher_invites
INSERT INTO teacher_invites (email, token_hash, expires_at, used_at, created_at, updated_at, deleted_at)
SELECT 'svetla.angelova@ppmg-vratsa.bg', SHA2('svetla-angelova-2026', 256),
       '2026-05-31 18:00:00', NULL, '2026-04-28 09:00:00', '2026-04-28 09:00:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM teacher_invites WHERE token_hash = SHA2('svetla-angelova-2026', 256));

INSERT INTO teacher_invites (email, token_hash, expires_at, used_at, created_at, updated_at, deleted_at)
SELECT 'ivan.rusev@ppmg-vratsa.bg', SHA2('ivan-rusev-2026', 256),
       '2026-05-31 18:10:00', NULL, '2026-04-28 09:10:00', '2026-04-28 09:10:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM teacher_invites WHERE token_hash = SHA2('ivan-rusev-2026', 256));

INSERT INTO teacher_invites (email, token_hash, expires_at, used_at, created_at, updated_at, deleted_at)
SELECT 'desislava.mitev@ppmg-vratsa.bg', SHA2('desislava-mitev-2026', 256),
       '2026-06-02 17:30:00', NULL, '2026-04-29 08:30:00', '2026-04-29 08:30:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM teacher_invites WHERE token_hash = SHA2('desislava-mitev-2026', 256));

INSERT INTO teacher_invites (email, token_hash, expires_at, used_at, created_at, updated_at, deleted_at)
SELECT 'petya.stefanova@ppmg-vratsa.bg', SHA2('petya-stefanova-2026', 256),
       '2026-04-20 17:00:00', NULL, '2026-04-18 10:00:00', '2026-04-18 10:00:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM teacher_invites WHERE token_hash = SHA2('petya-stefanova-2026', 256));

INSERT INTO teacher_invites (email, token_hash, expires_at, used_at, created_at, updated_at, deleted_at)
SELECT 'dimitar.vasilev@ppmg-vratsa.bg', SHA2('dimitar-vasilev-2026', 256),
       '2026-05-05 18:00:00', '2026-04-25 14:25:00', '2026-04-23 09:25:00', '2026-04-25 14:25:00', NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM teacher_invites WHERE token_hash = SHA2('dimitar-vasilev-2026', 256));
