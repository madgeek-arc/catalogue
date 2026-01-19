package gr.uoa.di.madgik.catalogue.ui.domain.types;

public sealed interface TypeProperties permits CustomProperties, DateProperties, NumberProperties, PatternProperties, TextProperties, UrlProperties, VocabularyProperties {
}
