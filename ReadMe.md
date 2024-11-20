## Meditronix 2.0
Meditronix 2.0 is an **updated GUI version of Meditronix** that was written in C with a terminal based interface.
Meditronix 2.0 builds up on its predecessor with robust data handling done using MySQL database instead of simple file handling .
The software is build using JavaFX & MySQL database & drivers.
All relevant project info and software structure can be found in the project documentation.


## **Currently Implemented Functionalities**

### **1. User Roles and Login**
- **Role-Based Login**:
  - **Shop Owner**: Access to inventory and sales functions.
  - **Doctor**: Access to prescription creation and historical data.
  - **Patient**: Access to view and purchase from the inventory.
- **Secure Login System**:
  - Credentials stored in the `users` table.

### **2. Features by User Role**
#### **Shop Owner**:
- View inventory specific to their shop location.
- Manage stock and update records.

#### **Doctor**:
- Create new prescriptions.
- View old prescriptions for patients.

#### **Patient**:
- Search and view prescriptions.
- Purchase items from a **generic inventory**.
- Make special purchases based on prescription limits.

### **3. Inventory Management**
- **Dynamic Inventory Display**:
  - Filtered by user profile location.
- **Comprehensive Data Representation**:
  - Includes details of available products.

---
## How to launch the application
Intellij project file structure was followed. We recommend using intellij IDE to launch the app

 - To enable database, you can go to database java class file and change the connection credentials in `dbConnect() ` method.
 - We recommend using MySQL for database handling since all sql statements were written with respect to  MySQL  syntax.
 - Login info is stored in the `users` table.
 - Currently no install wizard has been employed.
 - All table structure and example data are given in `meditronixDataExport.sql` file

## Contributors

 - Ahmed Rafid  | CSE | IUT | [AhmedRafid3S5](https://github.com/AhmedRafid3S5)
 - Rumman Adib  | CSE | IUT | [Rumman023](https://github.com/Rumman023)
 - Fariya Ahmed | CSE | IUT | [Fa-riya](https://github.com/Fa-riya)
