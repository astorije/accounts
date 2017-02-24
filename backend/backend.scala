// import java.time._

// Classes for Account and Transaction

// Leaving out date of transaction for now
// http://www.javapractices.com/topic/TopicAction.do?Id=13
case class Transaction(id: Int, /*date: DateTime,*/ description: String, amount: BigDecimal)

case class Account(id: Int, name: String, transactions: List[Transaction]) {
  def total: BigDecimal = transactions.map(_.amount).sum
}

object AccountRepo {
  // Hardcoded list of accounts and transactions for now
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

  // Ideally, this would be `createTransaction(transactionInput: TransactionInput)`
  // or `createTransaction(description: String, amount: BigDecimal)` so `id`
  // can be generated here or lower in the stack (DB auto-increment, ...)
  def createTransaction(transaction: Transaction) = { // FIXME
    // Commenting out id generation until I find a way to get id-free input
    // val r = scala.util.Random
    // val transaction = Transaction(r.nextInt(10000), description, amount)

    // For now, new transaction is added to first account. Later, an `accountId`
    // argument will be given to the `createTransaction` mutation.
    AccountRepo.accounts = List(
      accounts(0).copy(transactions = transaction :: accounts(0).transactions),
      accounts(1)
    )
    transaction
  }
}

// GraphQL definitions

import sangria.macros.derive._
import sangria.schema._
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
      description = Some("Returns an account for this id"),
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
    InputObjectTypeName("TransactionInput"),
    ExcludeInputFields("id")
  )
  val TransactionArg = Argument("transaction", TransactionInputType)

  val MutationType = ObjectType("Mutation", fields[AccountRepo, Unit](
    Field("createTransaction", TransactionType,
      description = Some("Create a new transaction for the first account"),
      arguments = TransactionArg :: Nil,
      resolve = c ⇒ c.ctx.createTransaction(
        c.arg(TransactionArg)
      )
    )
  ))

  val schema = Schema(QueryType, Some(MutationType))
}
