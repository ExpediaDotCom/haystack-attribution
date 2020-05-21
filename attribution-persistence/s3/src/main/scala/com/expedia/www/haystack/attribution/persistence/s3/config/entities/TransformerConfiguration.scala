package com.expedia.www.haystack.attribution.persistence.s3.config.entities

/**
  * Collection of transformer configurations. Consumers can write their own transformer and add/override it via config
  *
  * @param items Collection of transformer configs
  */
case class TransformerConfigurationList(items: List[TransformerConfiguration])

/**
  * Transformer configuration.
  *
  * @param id                unique id for this transformer
  * @param classRelativePath relative path of transformer class
  * @param customTags        map of tags that user wants to attach to the attributed s3 report
  */
case class TransformerConfiguration(id: String,
                                    classRelativePath: String,
                                    customTags: Map[String, String])