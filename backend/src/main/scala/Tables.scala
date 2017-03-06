import slick.jdbc.H2Profile.api._

class Accounts(tag: Tag) extends Table[Account](tag, "accounts") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")

  def * = (id, name) <> (Account.tupled, Account.unapply)
}

class Transactions(tag: Tag) extends Table[Transaction](tag, "transactions") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def description = column[String]("description", O.Length(512))
  def amount = column[BigDecimal]("amount")
  def accountId = column[Int]("user_id")

  def accountFk = foreignKey("user_fk", accountId, TableQuery[Accounts])(_.id) // FIXME Should be `accounts`?

  def * = (id, description, amount, accountId) <> (Transaction.tupled, Transaction.unapply)
}
