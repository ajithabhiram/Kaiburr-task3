Start the development server:

npm run dev


Open your browser and go to:
http://localhost:3001

💻 Usage
📊 Dashboard

View total tasks, executed tasks, and pending tasks

See a list of all tasks with their current status

📝 Create Task

Fill in the task form with ID, name, owner, and command

Click "Create Task" to save

⚙️ Manage Tasks

View all tasks in a list format

Search tasks by name, owner, or command

Execute tasks to run commands in Kubernetes

Edit existing tasks

Delete tasks when no longer needed

🖥️ Task Execution

Click "Execute" on any task to run its command

View the command output directly in the task list

See execution status (Executed / Not Executed)

🔗 API Integration

The frontend connects to the backend API at http://localhost:8081
 with these endpoints:

Method	Endpoint	Description
GET	/tasks	Get all tasks
PUT	/tasks	Create or update a task
DELETE	/tasks/{id}	Delete a task
PUT	/tasks/{id}/execute	Execute a task command
GET	/tasks/findByName	Search tasks by name
📁 Project Structure
src/
├── components/
│   ├── Dashboard.tsx       # Main dashboard page
│   ├── TaskForm.tsx        # Create/edit task form
│   └── TaskList.tsx        # Task listing and management
│
├── hooks/
│   └── useTasks.ts         # Custom hook for task operations
│
├── services/
│   └── api.ts              # API service functions
│
├── types/
│   └── index.ts            # TypeScript type definitions
│
├── utils/
│   └── index.ts            # Utility functions
│
├── App.tsx                 # Main application component
└── main.tsx                # Application entry point

🧾 Available Scripts
Command	Description
npm run dev	Start development server
npm run build	Build for production
npm run preview	Preview production build
npm run lint	Run code linting
⚙️ Configuration

The application uses Vite’s proxy configuration to forward API requests from
/api/* → http://localhost:8081.

🧩 Troubleshooting
🔌 Connection Issues

Ensure the backend API is running on port 8081

Confirm MongoDB is running (required by the backend)

Check firewall or proxy settings

🏗️ Build Issues

Delete node_modules and reinstall:

rm -rf node_modules
npm install


Ensure correct Node.js version is installed

⚠️ Runtime Errors

Check browser console for detailed error messages

Verify all required environment variables are properly set

📄 License: MIT
💡 Author: Abhiram Ajith
📦 Version: 1.0.0
