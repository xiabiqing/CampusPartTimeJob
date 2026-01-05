import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 引入Vant组件
import {
  NavBar,
  Form,
  CellGroup,
  Field,
  Button,
  Tabbar,
  TabbarItem,
  Cell,
  Empty,
  Search,
  Tag,
  Loading,
  Image as VanImage,
  Icon,
  Toast,
  Dialog,
  Popup,
  Picker,
  ActionSheet,
  Skeleton,
  PullRefresh,
  List,
  Tabs,
  Tab,
  ImagePreview
} from 'vant'
import 'vant/lib/index.css'

const app = createApp(App)
const pinia = createPinia()

// 注册Vant组件
app.use(NavBar)
app.use(Form)
app.use(CellGroup)
app.use(Field)
app.use(Button)
app.use(Tabbar)
app.use(TabbarItem)
app.use(Cell)
app.use(Empty)
app.use(Search)
app.use(Tag)
app.use(Loading)
app.use(VanImage)
app.use(Icon)
app.use(Toast)
app.use(Dialog)
app.use(Popup)
app.use(Picker)
app.use(ActionSheet)
app.use(Skeleton)
app.use(PullRefresh)
app.use(List)
app.use(Tabs)
app.use(Tab)
app.use(ImagePreview)

app.use(pinia)
app.use(router)

// 初始化用户信息（从localStorage恢复）
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
userStore.initUserInfo()

app.mount('#app')