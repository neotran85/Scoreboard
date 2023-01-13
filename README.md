
## download schema for role customer
```
./gradlew :app:downloadApolloSchema --endpoint='http://163.43.104.239:8087/v1/graphql' --schema=app/src/main/graphql/com/bidaappscoreboard/schema.graphqls --header="x-hasura-admin-secret: admin@1234" --header="x-hasura-role: customer"
```

## set apollo packageName
```
apollo {
    packageName.set("com.bidaappscoreboard")
}
```