UPDATE usuario SET correo = REPLACE(correo, '@nutrivida.cl', '@duocuc.cl') WHERE correo LIKE '%@nutrivida.cl';
UPDATE usuario SET genero = 'Mujer'  WHERE genero = 'Femenino';
UPDATE usuario SET genero = 'Hombre' WHERE genero = 'Masculino';