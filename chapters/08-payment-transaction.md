# Chapter 8: Payment and Transaction Systems

Payment systems handle financial transactions with security, reliability, and compliance requirements. This chapter covers two essential payment system designs: a general payment processing system and a specialized expense-sharing application like Splitwise.

## Problems Covered
1. Payment Processing System
2. Splitwise Application

---

## Problem 2: Splitwise Application

### Problem Statement

Design and implement a Splitwise-like application that allows users to track shared expenses, split bills among friends, and settle debts. The system should support multiple splitting methods, group management, and optimal settlement calculations.

### Requirements Analysis

**Functional Requirements:**
1. **User Management**: Register users, manage profiles, and handle user authentication
2. **Group Management**: Create groups, add/remove members, and manage group settings
3. **Expense Management**: Add expenses, categorize them, and track who paid what
4. **Expense Splitting**: Support multiple splitting methods (equal, exact amounts, percentages, shares)
5. **Balance Calculations**: Calculate who owes whom and by how much
6. **Settlement Management**: Create settlements, track payments, and optimize debt resolution
7. **Notifications**: Send payment reminders and expense notifications

**Non-Functional Requirements:**
1. **Consistency**: Ensure balance calculations are always accurate
2. **Performance**: Handle thousands of users and expenses efficiently
3. **Scalability**: Support growing user base and transaction volume
4. **Security**: Protect financial data and user privacy
5. **Reliability**: Ensure no money is lost or miscalculated

### System Design

#### Core Components

1. **User Management Service**: Handles user registration, authentication, and profile management
2. **Group Management Service**: Manages group creation, membership, and permissions
3. **Expense Management Service**: Handles expense creation, categorization, and tracking
4. **Balance Calculation Engine**: Computes balances and debt relationships
5. **Settlement Service**: Manages payment settlements and optimization
6. **Notification Service**: Sends reminders and updates to users

#### Data Models

**User Model:**
```java
public class User {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Group Model:**
```java
public class Group {
    private String groupId;
    private String name;
    private String description;
    private String createdBy;
    private Set<String> memberIds;
    private boolean isActive;
    private LocalDateTime createdAt;
}
```

**Expense Model:**
```java
public class Expense {
    private String expenseId;
    private String description;
    private BigDecimal totalAmount;
    private String paidBy;
    private String groupId;
    private ExpenseType type;
    private SplitType splitType;
    private Map<String, BigDecimal> splits;
    private String category;
    private String notes;
    private LocalDateTime createdAt;
}
```

**Settlement Model:**
```java
public class Settlement {
    private String settlementId;
    private String fromUserId;
    private String toUserId;
    private BigDecimal amount;
    private String groupId;
    private String description;
    private boolean isSettled;
    private LocalDateTime createdAt;
}
```

#### Key Algorithms

**1. Equal Split Algorithm:**
```java
public Map<String, BigDecimal> splitEqually(BigDecimal totalAmount, List<String> userIds) {
    BigDecimal splitAmount = totalAmount.divide(BigDecimal.valueOf(userIds.size()), 2, RoundingMode.HALF_UP);
    Map<String, BigDecimal> splits = new HashMap<>();
    
    for (String userId : userIds) {
        splits.put(userId, splitAmount);
    }
    
    // Handle rounding differences
    BigDecimal totalSplit = splits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal difference = totalAmount.subtract(totalSplit);
    
    if (difference.compareTo(BigDecimal.ZERO) != 0) {
        String firstUser = splits.keySet().iterator().next();
        splits.put(firstUser, splits.get(firstUser).add(difference));
    }
    
    return splits;
}
```

**2. Balance Calculation Algorithm:**
```java
public Map<String, BigDecimal> getGroupBalances(String groupId) {
    Map<String, BigDecimal> balances = new HashMap<>();
    
    // Initialize balances for all group members
    Group group = getGroup(groupId);
    for (String memberId : group.getMemberIds()) {
        balances.put(memberId, BigDecimal.ZERO);
    }
    
    // Calculate balances from expenses
    List<Expense> groupExpenses = getExpensesByGroup(groupId);
    for (Expense expense : groupExpenses) {
        String paidBy = expense.getPaidBy();
        BigDecimal totalAmount = expense.getTotalAmount();
        
        // Add the total amount to the payer's balance
        balances.put(paidBy, balances.get(paidBy).add(totalAmount));
        
        // Subtract each person's share from their balance
        for (Map.Entry<String, BigDecimal> split : expense.getSplits().entrySet()) {
            String userId = split.getKey();
            BigDecimal amount = split.getValue();
            balances.put(userId, balances.get(userId).subtract(amount));
        }
    }
    
    return balances;
}
```

**3. Optimal Settlement Algorithm:**
```java
public List<Settlement> calculateOptimalSettlements(String groupId) {
    Map<String, BigDecimal> balances = getGroupBalances(groupId);
    List<Settlement> settlements = new ArrayList<>();
    
    // Separate creditors and debtors
    List<Map.Entry<String, BigDecimal>> creditors = new ArrayList<>();
    List<Map.Entry<String, BigDecimal>> debtors = new ArrayList<>();
    
    for (Map.Entry<String, BigDecimal> entry : balances.entrySet()) {
        if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
            creditors.add(entry);
        } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
            debtors.add(entry);
        }
    }
    
    // Sort creditors by amount (descending) and debtors by amount (ascending)
    creditors.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    debtors.sort((a, b) -> a.getValue().compareTo(b.getValue()));
    
    // Create settlements using greedy algorithm
    int creditorIndex = 0;
    int debtorIndex = 0;
    
    while (creditorIndex < creditors.size() && debtorIndex < debtors.size()) {
        String creditor = creditors.get(creditorIndex).getKey();
        BigDecimal creditAmount = creditors.get(creditorIndex).getValue();
        
        String debtor = debtors.get(debtorIndex).getKey();
        BigDecimal debtAmount = debtors.get(debtorIndex).getValue().abs();
        
        BigDecimal settlementAmount = creditAmount.min(debtAmount);
        
        if (settlementAmount.compareTo(BigDecimal.ZERO) > 0) {
            Settlement settlement = new Settlement(generateId(), debtor, creditor, 
                                                 settlementAmount, groupId, "Optimal settlement");
            settlements.add(settlement);
            
            // Update remaining amounts
            creditAmount = creditAmount.subtract(settlementAmount);
            debtAmount = debtAmount.subtract(settlementAmount);
            
            creditors.get(creditorIndex).setValue(creditAmount);
            debtors.get(debtorIndex).setValue(debtAmount.negate());
        }
        
        // Move to next creditor or debtor
        if (creditAmount.compareTo(BigDecimal.ZERO) == 0) {
            creditorIndex++;
        }
        if (debtAmount.compareTo(BigDecimal.ZERO) == 0) {
            debtorIndex++;
        }
    }
    
    return settlements;
}
```

### Implementation Approach

#### 1. Service Layer Architecture

The implementation follows a layered architecture with clear separation of concerns:

- **Service Interface**: Defines the contract for Splitwise operations
- **Service Implementation**: Contains business logic and data management
- **Model Classes**: Represent domain entities with validation
- **Utility Classes**: Handle splitting algorithms and calculations

#### 2. Thread Safety

The implementation uses concurrent collections and atomic operations to ensure thread safety:

```java
private final Map<String, User> users = new ConcurrentHashMap<>();
private final Map<String, Group> groups = new ConcurrentHashMap<>();
private final Map<String, Expense> expenses = new ConcurrentHashMap<>();
private final AtomicInteger userIdCounter = new AtomicInteger(1);
```

#### 3. Data Consistency

Balance calculations are performed on-demand to ensure consistency:
- No cached balances that could become stale
- All calculations based on current expense and settlement data
- Validation to ensure splits equal total expense amounts

#### 4. Error Handling

Comprehensive validation and error handling:
- Input validation for all parameters
- Business rule validation (e.g., user must be group member)
- Graceful handling of edge cases (e.g., rounding differences)

### Key Features Implemented

#### 1. Multiple Splitting Methods

**Equal Split:**
- Divides expense equally among all participants
- Handles rounding differences automatically
- Most common splitting method

**Exact Amount Split:**
- Allows specifying exact amount for each person
- Useful when people ordered different items
- Validates that amounts sum to total

**Percentage Split:**
- Splits based on specified percentages
- Validates that percentages sum to 100%
- Useful for proportional sharing

**Shares Split:**
- Splits based on relative shares
- Flexible for different contribution levels
- Automatically calculates proportions

#### 2. Balance Management

**Group Balances:**
- Shows who owes money and who is owed
- Ensures balances always sum to zero
- Accounts for both expenses and settlements

**User Balances:**
- Shows user's position across all groups
- Helps identify overall debt/credit status
- Useful for personal financial tracking

**Pairwise Balances:**
- Calculates balance between any two users
- Considers all shared groups
- Useful for direct settlements

#### 3. Settlement Optimization

**Optimal Settlement Algorithm:**
- Minimizes number of transactions needed
- Uses greedy approach for efficiency
- Reduces complexity of debt resolution

**Settlement Tracking:**
- Records all payment settlements
- Tracks settlement status (pending/completed/rejected)
- Updates balances automatically

#### 4. Statistics and Reporting

**System Statistics:**
- Overall user and group activity
- Total expense amounts and settlement rates
- System health metrics

**Group Statistics:**
- Group activity and expense patterns
- Most active users and largest expenses
- Settlement efficiency metrics

**User Statistics:**
- Personal expense and payment history
- Activity across different groups
- Financial behavior insights

### Testing Strategy

The implementation includes comprehensive tests covering:

1. **Unit Tests**: Test individual methods and edge cases
2. **Integration Tests**: Test complete workflows
3. **Edge Case Tests**: Handle boundary conditions and error scenarios
4. **Performance Tests**: Validate system performance under load

### Usage Examples

#### Basic Usage Flow

```java
// 1. Create service and register users
SplitwiseService service = new InMemorySplitwiseService();
User alice = service.registerUser("Alice", "alice@example.com", "+1-555-0101");
User bob = service.registerUser("Bob", "bob@example.com", "+1-555-0102");

// 2. Create group and add members
Group roommates = service.createGroup("Roommates", "Apartment expenses", alice.getUserId());
service.addMemberToGroup(roommates.getGroupId(), bob.getUserId());

// 3. Add expense with equal split
Map<String, BigDecimal> splits = service.splitEqually(
    new BigDecimal("120.00"), 
    Arrays.asList(alice.getUserId(), bob.getUserId())
);
Expense groceries = service.addExpense(
    "Grocery shopping", new BigDecimal("120.00"), alice.getUserId(), 
    roommates.getGroupId(), ExpenseType.FOOD, SplitType.EQUAL, 
    splits, "Groceries", "Weekly groceries"
);

// 4. Check balances
Map<String, BigDecimal> balances = service.getGroupBalances(roommates.getGroupId());
// Alice: +60.00 (paid 120, owes 60)
// Bob: -60.00 (paid 0, owes 60)

// 5. Calculate optimal settlements
List<Settlement> settlements = service.calculateOptimalSettlements(roommates.getGroupId());
// Bob should pay Alice $60.00

// 6. Create and settle payment
Settlement payment = service.createSettlement(
    bob.getUserId(), alice.getUserId(), new BigDecimal("60.00"), 
    roommates.getGroupId(), "Grocery payment"
);
service.settlePayment(payment.getSettlementId());
```

### Advanced Features

#### 1. Complex Splitting Scenarios

```java
// Percentage-based split for vacation expenses
Map<String, BigDecimal> percentages = new HashMap<>();
percentages.put("alice", new BigDecimal("40"));  // 40%
percentages.put("bob", new BigDecimal("35"));    // 35%
percentages.put("charlie", new BigDecimal("25")); // 25%

Map<String, BigDecimal> splits = service.splitByPercentages(
    new BigDecimal("800.00"), percentages
);
```

#### 2. Multi-Group Balance Management

```java
// Get user's balances across all groups
Map<String, BigDecimal> userBalances = service.getUserBalances(alice.getUserId());
for (Map.Entry<String, BigDecimal> entry : userBalances.entrySet()) {
    String groupId = entry.getKey();
    BigDecimal balance = entry.getValue();
    System.out.println("Group " + groupId + ": " + 
                      (balance.compareTo(BigDecimal.ZERO) > 0 ? "owed" : "owes") + 
                      " $" + balance.abs());
}
```

#### 3. Settlement Optimization

```java
// Calculate optimal settlements for complex group
List<Settlement> optimalSettlements = service.calculateOptimalSettlements(groupId);
System.out.println("Minimum " + optimalSettlements.size() + " transactions needed:");
for (Settlement settlement : optimalSettlements) {
    System.out.println(settlement.getFromUserId() + " pays " + 
                      settlement.getToUserId() + " $" + settlement.getAmount());
}
```

### Best Practices and Considerations

#### 1. Financial Accuracy
- Use `BigDecimal` for all monetary calculations
- Handle rounding consistently across all operations
- Validate that splits always equal total amounts
- Maintain audit trails for all financial operations

#### 2. Data Consistency
- Ensure balances are calculated from source data
- Validate business rules at service layer
- Use transactions for multi-step operations
- Implement proper error handling and rollback

#### 3. Performance Optimization
- Use efficient data structures (HashMap, ConcurrentHashMap)
- Minimize database queries through caching
- Optimize balance calculations for large groups
- Consider pagination for large result sets

#### 4. Security Considerations
- Validate user permissions for all operations
- Sanitize input data to prevent injection attacks
- Implement proper authentication and authorization
- Protect sensitive financial information

#### 5. Scalability Planning
- Design for horizontal scaling
- Consider database sharding strategies
- Implement caching for frequently accessed data
- Plan for eventual consistency in distributed systems

### Common Pitfalls and Solutions

#### 1. Rounding Errors
**Problem**: Decimal division can lead to rounding errors that don't sum to total
**Solution**: Always adjust the first split to handle rounding differences

#### 2. Concurrent Modifications
**Problem**: Multiple users modifying the same group simultaneously
**Solution**: Use concurrent collections and atomic operations

#### 3. Complex Debt Cycles
**Problem**: Circular debts that are hard to resolve
**Solution**: Implement optimal settlement algorithm to minimize transactions

#### 4. Data Inconsistency
**Problem**: Cached balances becoming stale
**Solution**: Calculate balances on-demand from source data

### Conclusion

The Splitwise application demonstrates key concepts in financial system design:

1. **Accurate Financial Calculations**: Using proper decimal arithmetic and rounding
2. **Complex Business Logic**: Implementing multiple splitting algorithms and settlement optimization
3. **Data Consistency**: Ensuring balances always reflect current state
4. **User Experience**: Providing intuitive APIs for common operations
5. **Scalability**: Designing for growth in users and transactions

This implementation provides a solid foundation for building expense-sharing applications and demonstrates important patterns for financial software development. The modular design allows for easy extension with additional features like recurring expenses, expense categories, and integration with payment systems.