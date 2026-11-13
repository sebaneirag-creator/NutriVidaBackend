INSERT INTO `categoria` (`nombre`) VALUES
    ('Consulta'),
    ('Plan especializado'),
    ('Evaluación'),
    ('Taller grupal');

INSERT INTO `servicio` (`codigo`, `nombre`, `duracion`, `modalidad`, `precio`, `stock`, `descripcion`, `id_categoria`) VALUES
    ("CN001", "Primera consulta nutricional", "50 min", "PRESENCIAL", 35000, 5,"Evaluación inicial: anamnesis, antropometría completa y diseño del primer plan alimenticio.", 1),
    ("CN002", "Control nutricional (seguimiento)", "30 min", "PRESENCIAL", 25000, 5, "Seguimiento mensual: medición de indicadores y ajuste del plan vigente.", 1),
    ("CN003", "Control nutricional quincenal", "30 min", "PRESENCIAL", 22000, 5, "Seguimiento intensivo cada 15 días. Recomendado en los primeros 2 meses.",1 ),
    ("CN004", "Teleconsulta nutricional", "30 min", "ONLINE_VIDEO", 20000, 5, "Consulta de seguimiento vía videollamada. Requiere contar con consulta presencial previa.",1 ),
    ("CN005", "Consulta de urgencia / reagendada", "30 min", "PRESENCIAL", 28000, 5, "Para pacientes que requieren atención fuera de su control habitual.", 1),

    ("PL001", "Plan pérdida de peso (1 mes)", "—", "PRESENCIAL", 65000, 3, "Incluye primera consulta + 1 control quincenal + plan alimenticio personalizado + seguimiento por WhatsApp.", 2),
    ("PL002", "Plan pérdida de peso (3 meses)", "—", "PRESENCIAL", 170000, 3, "Incluye primera consulta + 5 controles + 3 planes mensuales + seguimiento continuo.", 2),
    ("PL003", "Plan nutrición deportiva (1 mes)", "—", "PRESENCIAL", 70000, 3, "Para deportistas y personas con actividad física frecuente. Cálculo de requerimientos energéticos y proteicos.", 2),
    ("PL004", "Plan control diabetes / hipertensión", "—", "PRESENCIAL", 75000, 3, "Plan adaptado para patologías metabólicas. Coordinación con médico tratante si aplica.", 2),
    ("PL005", "Plan alimentación vegetariana/vegana", "—", "PRESENCIAL", 68000, 3, "Diseñado para garantizar aporte adecuado de proteínas, hierro, vitamina B12 y calcio sin productos animales.", 2),
    ("PL006", "Plan alimentación infantil (2-12 años)", "—", "PRESENCIAL", 65000, 3, "Evaluación nutricional pediátrica y diseño de plan adaptado a la etapa de desarrollo del niño.", 2),

    ("EV001", "Antropometría completa", "20 min", "PRESENCIAL", 18000, 8, "Peso, talla, IMC, circunferencia de cintura, cadera, brazo y % de grasa corporal con bioimpedanciometría.", 3),
    ("EV002", "Bioimpedanciometría", "15 min", "PRESENCIAL", 12000, 8, "Medición de composición corporal: masa grasa, masa muscular, agua corporal y edad metabólica.", 3),
    ("EV003", "Encuesta de hábitos alimentarios", "20 min", "PRESENCIAL", 10000, 8, "Análisis del patrón alimentario actual. Identificación de déficit y excesos nutricionales.", 3),
    ("EV004", "Análisis de exámenes de laboratorio", "20 min", "PRESENCIAL", 15000, 8, "Interpretación de hemograma, perfil bioquímico y lipídico en contexto nutricional.", 3),

    ("TG001", "Taller de alimentación saludable", "90 min", "PRESENCIAL_GRUPO", 15000, 10, "Máx. 10 personas. Conceptos básicos de alimentación equilibrada y lectura de etiquetas.", 4),
    ("TG002", "Taller de cocina nutritiva", "120 min", "PRESENCIAL_GRUPO", 20000, 8, "Preparación de recetas saludables. Incluye degustación. Máx. 8 personas.", 4),
    ("TG003", "Taller nutrición para deportistas", "90 min", "PRESENCIAL_GRUPO", 18000, 12, "Hidratación, nutrición pre y post entrenamiento, suplementación básica. Máx. 12 personas.", 4);