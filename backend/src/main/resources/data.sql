
INSERT INTO bank (blz, name, bic) VALUES
  ('37040044', 'Commerzbank',        'COBADEFFXXX'),
  ('37050198', 'Sparkasse KölnBonn', 'COLSDE33XXX'),
  ('37070060', 'Deutsche Bank',      'DEUTDEDKXXX')
ON CONFLICT (blz) DO NOTHING;      