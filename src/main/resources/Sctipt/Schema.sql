USe payroll_system;

USE payroll_system;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE employee_profiles;
TRUNCATE TABLE users;
TRUNCATE TABLE attendances;
TRUNCATE TABLE payrolls;


SET FOREIGN_KEY_CHECKS = 0;

USE payroll_system;
SELECT @@global.time_zone, @@session.time_zone;

# USE payroll_system;
# DELETE from users where id = 1;