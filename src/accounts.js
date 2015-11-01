var data = {
  accounts: [
    "Checking Account",
    "Savings Account",
  ],
};

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
      <div>
        <AccountList accounts={this.state.data.accounts} />
        <AccountForm onAccountSubmit={this.handleAccountSubmit} />
      </div>
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

var AccountForm = React.createClass({
  handleSubmit: function(e) {
    e.preventDefault();
    var name = this.refs.name.value.trim();
    if (!name) {
      return;
    }
    // TODO: send request to the server
    this.props.onAccountSubmit({ name: name });
    this.refs.name.value = '';
    return;
  },
  render: function() {
    return (
      <form className="commentForm" onSubmit={this.handleSubmit}>
        <input type="text" placeholder="Account name" ref="name" />
        <input type="submit" value="Add account" />
      </form>
    );
  }
});

ReactDOM.render(
  <Accounts data={data} />,
  document.getElementById('accounts')
);
