package at.syntaxerror.json5

sealed class UnicodeCharacter(
  val char: Char,
  val representation: String,
) {

  object VerticalTab : UnicodeCharacter('\u000B', """\u000B""")
  /** `\f` */
  object FormFeed : UnicodeCharacter('\u000c', "\\v")

}