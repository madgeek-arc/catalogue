DO $$
BEGIN
    IF to_regclass('public.resourcetype_properties') IS NOT NULL THEN
        UPDATE resourcetype_properties
        SET properties = replace(
                properties,
                'gr.uoa.di.madgik.catalogue.ui.domain.',
                'gr.uoa.di.madgik.catalogue.domain.'
                         )
        WHERE properties_key = 'class' AND properties LIKE 'gr.uoa.di.madgik.catalogue.ui.domain.%';
    END IF;
END $$;
