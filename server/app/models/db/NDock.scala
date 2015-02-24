package models.db

import models.join.{NDockWithDeckId, NDockWithName}
import scalikejdbc._
import com.ponkotuy.data

/**
 *
 * @author ponkotuy
 * Date: 14/03/02.
 */
case class NDock(id: Int, memberId: Long, shipId: Int, completeTime: Long, created: Long)

object NDock extends SQLSyntaxSupport[NDock] {

  def apply(x: SyntaxProvider[NDock])(rs: WrappedResultSet): NDock = apply(x.resultName)(rs)
  def apply(x: ResultName[NDock])(rs: WrappedResultSet): NDock = autoConstruct(rs, x)

  lazy val nd = NDock.syntax("nd")
  lazy val s = Ship.syntax("s")
  lazy val ms = MasterShipBase.syntax("ms")
  lazy val ds = DeckShip.syntax("ds")

  def findAllByUser(memberId: Long)(implicit session: DBSession = NDock.autoSession): List[NDock] = withSQL {
    select.from(NDock as nd)
      .where.eq(nd.memberId, memberId)
  }.map(NDock(nd)).toList().apply()

  def findAllByUserWithName(memberId: Long)(implicit session: DBSession = NDock.autoSession): List[NDockWithName] =
    withSQL {
      select(nd.id, nd.memberId, nd.shipId, nd.completeTime, nd.created, ms.id, ms.name).from(NDock as nd)
        .innerJoin(Ship as s).on(sqls"${nd.memberId} = ${s.memberId} and ${nd.shipId} = ${s.id}")
        .innerJoin(MasterShipBase as ms).on(s.shipId, ms.id)
        .where.eq(nd.memberId, memberId)
        .orderBy(nd.id)
    }.map { rs =>
      NDockWithName(
        rs.int(nd.id),
        rs.long(nd.memberId),
        rs.int(nd.shipId),
        rs.long(nd.completeTime),
        rs.long(nd.created),
        rs.int(ms.id),
        rs.string(ms.name))
    }.toList().apply()

  def findAllByUserWithDeckId(memberId: Long)(implicit session: DBSession = NDock.autoSession): List[NDockWithDeckId] =
    withSQL {
      select(nd.id, nd.memberId, nd.shipId, nd.completeTime, nd.created, ds.deckId).from(NDock as nd)
        .leftJoin(DeckShip as ds).on(nd.shipId, ds.shipId)
        .where.eq(nd.memberId, ds.memberId)
        .orderBy(nd.id)
    }.map { rs =>
      NDockWithDeckId(
        rs.int(nd.id),
        rs.long(nd.memberId),
        rs.int(nd.shipId),
        rs.long(nd.completeTime),
        rs.long(nd.created),
        rs.intOpt(ds.deckId)
      )
    }.toList().apply()

  def create(nd: data.NDock, memberId: Long)(implicit session: DBSession = NDock.autoSession): Unit = {
    val created = System.currentTimeMillis()
    applyUpdate {
      insert.into(NDock).namedValues(
        column.id -> nd.id,
        column.memberId -> memberId,
        column.shipId -> nd.shipId,
        column.completeTime -> nd.completeTime,
        column.created -> created
      )
    }
  }

  def deleteAllByUser(memberId: Long)(implicit session: DBSession = NDock.autoSession): Unit =
    applyUpdate { delete.from(NDock).where.eq(NDock.column.memberId, memberId) }
}
