package io.github.jmecn.font.delegate;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Limits;
import com.jme3.texture.Texture;
import com.simsilica.lemur.*;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.enums.RenderMode;
import io.github.jmecn.font.plugins.FtFontKey;
import io.github.jmecn.font.plugins.FtFontLoader;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestLemur extends SimpleApplication {

    public static void main(String[] args) {
        TestLemur app = new TestLemur();
        app.start();
    }
    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        // 获取GPU兼容性数据，确定最大纹理大小
        Integer textureSize = getRenderer().getLimits().get(Limits.TextureSize);
        FtFontGenerator.setMaxTextureSize(Math.min(textureSize, 4096));

        String payload = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwx«·»αδσ“”　【】一丁七万丈三上下不与丑专世丘丙业丛东丝丢两严个中丰串临丸丹为主丽乃久义之乌乐乔乘乙九习乡书乱乳争事二于云五井亚亡交亥产亨享京亭亮亲亵人什仁仆仇今从仑仔他仗仙代令以仪们仰件仿企伊伍伏伐休众优伙会伞伟传伤伦伪伯伴似伽位低住佐佑体何余佛作你佣佩佳使侏供依侠侣侧便促俄保信修俾倌倒倔候值倾偃假偏做停健偶偷傅储催傲像僧僵儒儿兀元兄充兆先光克兑兔党兜入全八公六兰共关兴兵其具典兹养兼兽内冈册再冒写军农冠冥冬冰冲决冶冷冻净准凇凉凌减凛凝几凡凤凭凯凰凳凶凸出击刀刁刃分切刊刑划列刘则刚创初判刨利别到制刷券刹刺刻剂剃削剌前剑剖剧剪副割力功加务动助努劫励劲劳劵势勃勇勒勺勿包匕化北匙匠区医匿十千升午半华协卒卓单卖南博卜占卡卢卤卦卧卫印危即却卵卷卸卿厂厄厅历厉压厌厚原厥厨去叁参叉及友双反发叔取受变叙叛叠口古另叩叫召叭叮可台史右叶号司叹叽吃合吉吊同名后吐向吒吓吕君吞吟吧听启吱吸吹吻吼呀告呐员呢周味呵呼命咆和咏咒咕咖咧咪咬咸咻哀品哈响哎哑哔哟哥哨哩哪哭哮哲唇唐唤唬唯唱唾商啡啦啪啵啸喀善喇喊喙喜喝喧喱喷嗜嗡嘈嘉嘎嘴嘿噌噗器噩噪噬嚎嚣囊四回因团园困囱围固国图圃圆圈土圣在地场圾坎坏坐坑块坚坛坠坦坩坪坯垂垃型垓垢垩垫垮埃埋城埚域埠培基堂堆堇堕堡堤塑塔塘塞填境墅墓墙增墟墨墩壁壑壤士壬壮声壳壶壹处备复夏夕外多夜大天太夫央失头夹夺奇奈奉奋奏契奔奖套奥女奴奶奸好如妃妄妆妇妈妒妖妙妨妮妹妻姆始姐姑委姜姥姬威娃娄娅娇娑娘娜娟婆婚婴媒嫉嫩孀子孔字存孙孚孜孢季孤学孩宁宇守安完宗官宙定宛宝实宠审客宣室宫宰害宴宵家容宽宾宿寂寄密富寒寓寝察寡寨寸对寺寻导寿封射将尉尊小少尔尖尘尚尤就尸尼尽尾局层居屈屉屋屏屐屑展属屠履山屿岁岑岗岛岩岭岳岸峙峰峻崇崖崩崽巅川州巡巢工左巧巨巫差己已巴巷巾币市布帅帆师希帐帕帘帚帜帝带席帮帷常帽幅幔幕干平年幸幻幼幽广庄庆庇床序库应底店庙府庞废度座庭庵康庸廊延廷建开弁异弃式弓引弗弟张弥弦弧弯弱弹强归当录彗形彦彩彭影役彻彼往征径待徊律徒得徘御循微德徹徽心必忆忌忍忒志忘忠忧快念忾怀态怒怕思急性怨怪怯总恋恐恒恕恢恨恩息恰恶恻恼悄悌悍悔悟悠悦悬悯悲悼情惊惋惑惜惠惧惩惬惭惰想愁愈愉意愕愚感愤愿慈慌慎慕慢慧慰憎憩懒戈戌戎戏成我戒或战戟戮戴户房所扁扇手才扎打托扣执扩扫扬扰扶找承技抄把抑抓投抗折抚抛护报披抱抹抽拂担拉拌拐拓拔拖拘招拜拟拥拨拱拳拼拾拿持挂指挑挖挚挛挞挥振挽捂捌捕捞损换捣捧据捷掉掌排掘掠探掣接控推掷描提插握援搏搓搭携摆摇摊摘摧摩摹撑撒撕撞撩撼操擎擘擦攀攫支收改攻放故效敌敏救敖教散敬数整文斋斐斑斓斗料斜斥斧斩断斯新方施旅旋族旗无日旧早旭时昂昆昌明昏易昔星春昭是昴显晋晒晓晕晖晚晨普景晴晶智暂暖暗暮暴曙曜曦曲曳更曼替最月有服朗望朝期木未末本札术朱朴朵机朽杀杂权杆杉杏材村杖杜束条来杨杯杰杵杷松板极构枇析林果枝枢枪枫枭枯架柄柏柒染柔柚柜柠查柩柬柯柱柳柴柿栀栅标栌栎栏树栓栖栗校样核根格栽桂桃桅框案桉桌桑桓桔桤桥桦桨桩桶梁梅梣梦梨梭梯械梳梵检棉棋棍棒棕棘森棱棺椅植椒椭椰楔楚楠楯楼概榄榆榈榉榊榍榧榴榻槁槃槌槎槲槽樟模横樱樵橄橘橙橡橱檀檐檬欠次欢欣欧欲款歇歌止正此步武歹死歼殇殊残殖殴殿毅母毒比毕毗毛毡毯氏民气水永汀汁求汃汇汉汕汗江池汤汪汽沃沉沌沙沟没沥沧沫河沸油治沼沿泉法泛泡波泣泥注泪泰泳泷泽洁洄洋洗洛洞津洪洲活洽派流浅浆浇测济浑浓浦浪浮浴海浸涂涅消涌涎涛涟涡涤液淋淑淘淙淡淤淬淮深混添清渊渍渎渔渗渠渡渣温港渴游湖湛湾湿溃源溜溶溺滑滚滞满滤滨滩滴漂漆漏演漠漩漪漫潘潜潮澈激瀑灌火灭灯灰灵灼灾灿炀炉炎炒炖炙炬炭炮炸点炼炽烁烂烈烘烛烟烤烦烧烩热烷烹烽焕焖焗焙焚焦焰然煌煎煞煤照煮煽熊熏熔熙熟熠熨燃燕燥燧爆爪爬爱爵父爸爹爽片版牌牙牛牡牢牦牧物牲牵特牺犀犊犏犬犯犰状犷犸犹狂狄狆狐狗狛狞狩独狭狮狰狱狳狸狼猁猎猛猞猥猩猪猫猬献猴猿獭獴玄率玉王玕玖玛玩玫环现玲玳玺玻珀珂珊珍珞珠班球琅理琉琥琪琳琴琵琶瑁瑕瑙瑚瑞瑟瑰瑶璃璎瓜瓣瓦瓶瓷甘甜生用田由甲电男甸画界畏畔留畜略番疏疑疗疣疫疮疯疾病症痉痊痕痛痹瘟癸登白百皂的皇皎皮皱盆益盏盐监盒盔盖盗盘盛盟目盲直相盾省看真眠眩眼着睛睡督睿瞪瞬瞭瞳矛矢知矩短矮石矶矾矿码砂砌砍研砖砧破砾础硅硌硕硝硫硬确硼碍碎碑碗碟碧碱碳磁磨磷磺礁示礼祇祈祖祝神祠祥票祭祷祸禁禄福离禽秀私秃秆秋种科秘秤秦秧积移稀程稍税稳稻稽稿穆穗穴究穹空穿突窑窒窗窜窟窥立竖站竞章竣童端竹竺竿笃笆笋笔笛笠符第笼等筋筐筑筒答策筝简箍箔箕算管箭箱箴篇篝篮篱篷簇簧簪籍米类籽粉粒粕粗粘粥粮粳精糊糕糖糯系素索紧紫繁纠红纤约级纪纫纯纱纲纳纷纸纹纺纻纽线绀练组绅细织终绊绍经绑绒结绕绘给绚绛络绝绞统绢绣继绩绮绯绳维绵绷绸绽绿缇缉缌缎缓缕编缚缝缟缠缤缩缪缭缸缺罐网罕罗罚罩罪置署羁羊美羔羚群羯羹羽翁翅翔翘翠翡翱翻翼耀老考者而耐耕耗耳耶耽聆职联聚聪肆肉肖肘肝肠肤肥肩肮肯肴肺胄胆背胖胜胡胧胫胭胴胶胸能脂脉脊脏脑脚脯脱脸腌腐腔腕腥腮腰腱腹腾腿膀膏膜膝臀臂臣自臭至致舄舌舍舞舟航般舰舵舶舷船艇良艰色艳艺艾节芋芒芙芜芝芥芦芬芭芯花芳芸芹芽苍苎苏苑苔苗苣若苦英苹范茄茉茎茧茨茫茱茴茵茶茸茹荆草荐荒荞荟荠荡荣荧荨荫药荷莉莎莓莫莱莲莴获莺莽菁菇菊菌菜菠菩菰菱菲萃萄萌萍萎萝萤营萨落著葛葡董葫葬葱葵蒂蒙蒜蒲蒸蒿蓄蓉蓑蓓蓝蓟蓬蔓蔗蔚蔬蔷蔻蔽蕉蕨蕴蕾薄薇薙薯薰藏藓藕藤藻蘑虎虑虔虚虫虱虹虾蚁蚂蚊蚌蚓蚕蚝蚤蚪蚬蚯蚱蚺蛀蛄蛇蛉蛋蛎蛙蛛蛞蛟蛤蛭蛮蛹蛾蜂蜃蜉蜊蜍蜓蜗蜘蜜蜡蜢蜥蜴蜻蝇蝉蝌蝎蝓蝙蝠蝣蝰蝲蝴蝶蝼蝾螃螅螈融螠螯螺蟒蟥蟹蟾蠕蠢血衅行街衡衣补表衫衬衮衰袈袋袍袖袜被袭袴裁裂装裈裔裘裙裟裤裳裸裹褂褐褥褪褶西要覆见观规视觉角解触言誉誓警计订认讨让训议讯记讲讳讴许论设证诃诅识诈词试诗诚话诞诡详诫语诱诲说请诸诺读调谊谋谎谐谕谜谢谣谭谱谷豁豆豌豚象豪豹豺貘贝贞负贡财责贤败货质贪购贯贰贴贵贸费贺贻贼贾资赋赌赎赏赐赖赛赞赤赦赫赭走赶起超越趣足趾跃跑跟跨跪路跳踏踝踪踵蹄蹈蹦蹼身车轨转轮软轰轴轻载轿辅辇辉输辛辞辣辨辩辫辰边辽达迅过迎运近返还进远连迦迪迭述迷迸迹追退送适逃逆选透逐途通逝速造逢遁遂遇道遗遥遮避那邦邪邮邸郁郎部都配酒酥酪酬酱酵酷酸酿醉醋醒采釉释里重野量金鉴鍪鎏针钉钐钒钓钛钝钟钢钥钧钨钩钯钱钳钴钵钻钼钿铁铂铃铅铆铋铍铐铑铛铜铝铠铬铭铲铳银铸铺链锁锂锄锅锆锈锉锋锌锐锔错锚锡锤锥锦锭键锯锰锻镀镁镂镇镐镔镖镜镣镯镰镴镶长门闪闭问闲间闹闻阀阁阅阎阑阔队阱防阳阴阵阶阿陀附际陆陈降限院除陨险陵陶陷隆随隐隔隼难雀雄雅集雇雉雌雏雕雨雪雯雳零雷雾需霆震霉霍霓霜露霸霹霾青静靛非靠面革靴靶鞋鞍鞘鞠鞣鞭韦韧韭音韵页顶项顺须顿颂预颅领颈颊颌颏题颚额颠风飒飘飞食餐饪饭饮饯饰饲饵饶饼饿馅馆馏馒首香馨马驭驯驱驶驻驼驾驿骄骆骇验骏骑骗骤骨骰骷骸骼髅髓高髦髻鬃鬣鬼魂魅魇魏魔魟鮄鮈鮟鮣鮨鯙鯥鰆鰤鱇鱊鱲鱵鱼鱿鲀鲁鲂鲃鲅鲆鲈鲉鲎鲑鲛鲜鲡鲢鲣鲤鲦鲨鲫鲮鲯鲱鲴鲶鲷鲸鲹鲻鲼鲽鳀鳃鳄鳅鳉鳌鳍鳎鳐鳕鳖鳗鳝鳞鳟鳢鸟鸡鸢鸣鸥鸦鸭鸮鸽鹃鹄鹅鹉鹊鹤鹦鹫鹰鹳鹿麒麓麝麟麦麻黄黎黏黑默黛黝黯鼓鼠鼬鼯鼹鼻齐齿龄龙龟！＆（），／：？～";

        // 加载字体
        assetManager.registerLocator(".", FileLocator.class);
        assetManager.registerLoader(FtFontLoader.class, "ttf", "otf");
        FtFontKey key = new FtFontKey("font/NotoSerifSC-Regular.otf", 14, true);
        key.setCharacters(payload);
        key.setRenderMode(RenderMode.SDF);
        key.setMatDefName("Shaders/Font/SdFont.j3md");
        key.setUseVertexColor(false);
        key.setMagFilter(Texture.MagFilter.Bilinear);
        key.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        BitmapFont fnt = assetManager.loadAsset(key);

        System.out.println("Font loaded!");

        // 初始化lemur
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        GuiGlobals.getInstance().getStyles().setDefault(fnt);

        System.out.println("Lemur initialized!");

        Label title = new Label("你好~！");

        TextField text = new TextField("随便输入一些文字");
        text.setSingleLine(false);

        Button clickMe = new Button("点我");
        clickMe.addClickCommands(source -> System.out.println("The world is yours."));

        // 创建测试窗口
        Container myWindow = new Container();
        myWindow.addChild(title);
        myWindow.addChild(text);
        myWindow.addChild(clickMe);
        myWindow.setLocalTranslation(300, 300, 0);
        guiNode.attachChild(myWindow);
    }
}
