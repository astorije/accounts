var data = {
  accounts: [
    "Checking Account",
    "Savings Account",
  ],
};

var operations = [
  { date: new Date().toTimeString(), description: 'Test', amount: 1},
  { date: new Date().toTimeString(), description: 'Test', amount: 2},
  { date: new Date().toTimeString(), description: 'Test', amount: 3},
];

var Operations = React.createClass({
  remove: function (line) {
    return function (e) {
      e.preventDefault();

      this.props.onRemove(line);
    }.bind(this);
  },
  render: function () {
    var lines = this.props.data.map(function (line) {
      return (
        <tr>
          <td>
            <a href="" onClick={this.remove(line)}>
              <span className="glyphicon glyphicon-remove"></span>
            </a>
          </td>
          <td>{line.date}</td>
          <td>{line.description}</td>
          <td>{line.amount}</td>
        </tr>
      );
    }.bind(this));

    return (
      <table className="table table-hover">
        <tbody>
          {lines}
        </tbody>
      </table>
    );
  }
});

var OperationForm = React.createClass({
  handleSubmit: function(e) {
    e.preventDefault();

    var date = this.refs.date.value.trim();
    var description = this.refs.description.value.trim();
    var amount = this.refs.amount.value.trim();

    if (!date || !description || !amount) {
      return;
    }
    // TODO: send request to the server
    this.props.onOperationSubmit({
      date: date,
      description: description,
      amount: amount,
     });

    this.refs.date.value = '';
    this.refs.description.value = '';
    this.refs.amount.value = '';
    return;
  },
  render: function() {
    return (
      <form className="commentForm" onSubmit={this.handleSubmit}>
        <input type="text" placeholder="Date" ref="date" />
        <input type="text" placeholder="Description" ref="description" />
        <input type="text" placeholder="Amount" ref="amount" />
        <input type="submit" value="Add" />
      </form>
    );
  }
});

var OperationManager = React.createClass({

  getInitialState: function() {
    return this.props;
  },

  handleOperationSubmit: function (operation) {
    var operations = this.state.data;
    operations.unshift(operation);
    this.setState({ data: operations });
  },
  handleOperationRemove: function (line) {
    var newOperations = this.state.data.filter(function (item) {
      return item.date !== line.date ||
        item.description !== line.description ||
        item.amount !== line.amount;
    });
    this.setState({ data: newOperations });
  },
  render: function () {
    return (
      <div>
        <OperationForm onOperationSubmit={this.handleOperationSubmit} />
        <Operations data={this.state.data}
          onRemove={this.handleOperationRemove} />
      </div>
    );
  }
});

ReactDOM.render(
  <OperationManager data={operations} />,
  document.getElementById('operations')
);

var Accounts = React.createClass({
  handleAccountSubmit: function (account) {
    var accounts = this.state.data.accounts;
    var newAccounts = accounts.concat([account.name]);
    this.setState({ data: { accounts: newAccounts } });
  },
  getInitialState: function() {
    return this.props;
  },
  render: function () {
    return (
      <AccountList accounts={this.state.data.accounts} />
    );
  }
});

var AccountList = React.createClass({
  render: function() {
    var accountNodes = this.props.accounts.map(function (account) {
      return (
        <li>{account}</li>
      );
    });

    return (
      <ul>
        {accountNodes}
      </ul>
    );
  }
});

ReactDOM.render(
  <Accounts data={data} />,
  document.getElementById('accounts')
);
