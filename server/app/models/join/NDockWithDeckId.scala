package models.join

case class NDockWithDeckId(
  id: Int,
  memberId: Long,
  shipId: Int,
  completeTime: Long,
  created: Long,
  deckId: Option[Int])