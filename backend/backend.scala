


// import java.time._

// http://www.javapractices.com/topic/TopicAction.do?Id=13
case class Transaction(id: Int, /*date: DateTime,*/ description: String, amount: BigDecimal)

case class Account(id: Int, name: String, transactions: List[Transaction]) {
  def total: BigDecimal = transactions.map(_.amount).sum
}

import sangria.macros.derive._
import sangria.schema._


object AccountRepo {
  var accounts = List(
    Account(1, "Checking Account", List(
      Transaction(1, "MBTA", BigDecimal("22.5")),
      Transaction(2, "Uber", BigDecimal("10.23"))
    )),
    Account(2, "Savings Account", List(
      Transaction(3, "Interests", BigDecimal("0.34"))
    ))
  )
}

class AccountRepo {
  def accounts: List[Account] = AccountRepo.accounts

  def account(id: Int): Option[Account] = accounts.find(_.id == id)

  def addTransaction(transaction: Transaction) = {
    // val r = scala.util.Random
    // val transaction = Transaction(r.nextInt(10000), description, amount)
    AccountRepo.accounts = List(
      accounts(0).copy(transactions = transaction :: accounts(0).transactions),
      accounts(1)
    )
    transaction
  }
}

// import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import sangria.marshalling.sprayJson._
import spray.json._
import DefaultJsonProtocol._

object AccountSchema {
  val Id = Argument("id", IntType)

  implicit val TransactionType = deriveObjectType[Unit, Transaction](
    ObjectTypeDescription("A transaction")
  )

  val AccountType = deriveObjectType[Unit, Account](
    ObjectTypeDescription("An account"),
    IncludeMethods("total")
  )

  val QueryType = ObjectType("Query", fields[AccountRepo, Unit](
    Field("account", OptionType(AccountType),
      description = Some("Returns an account with specific `id`"),
      arguments = Id :: Nil,
      resolve = c ⇒ c.ctx.account(c.arg(Id))
    ),

    Field("accounts", ListType(AccountType),
      description = Some("Returns a list of all available accounts"),
      resolve = _.ctx.accounts
    )
  ))

  implicit val transactionFormat = jsonFormat3(Transaction)
  val TransactionInputType = deriveInputObjectType[Transaction](
    InputObjectTypeName("TransactionInput")
  )
  val TransactionArg = Argument("transaction", TransactionInputType)

  val MutationType = ObjectType("Mutation", fields[AccountRepo, Unit](
    Field("addTransaction", TransactionType,
      arguments = TransactionArg :: Nil,
      resolve = c ⇒ c.ctx.addTransaction(
        c.arg(TransactionArg)
      )
    )
  ))

  val schema = Schema(QueryType, Some(MutationType))
}
